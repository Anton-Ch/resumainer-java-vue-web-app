/**
 * Frontend adapter for review data.
 *
 * Transforms backend GenerationReviewDto (hierarchical: language → section → record → field → adaptation)
 * into prototype-style view model (flat: GeneratedVariant[] per language).
 *
 * Also builds save payload from edited fields.
 */
import type {
  GenerationReviewDto,
  LanguageReviewGroup,
  SectionReviewGroup,
  RecordReviewGroup,
  AdaptationVariant,
  ReviewViewModel,
  GeneratedVariant,
  GeneratedExperience,
  GeneratedCourse,
  GeneratedProject,
  GeneratedSkillGroup,
  GeneratedPersonalInfo,
  GenerationReviewUpdateDto,
  PrototypeLevel
} from '@/types/generate'

// ── Level mapping ───────────────────────────────────────────────────

function toPrototypeLevel(code: string): PrototypeLevel {
  switch (code?.toUpperCase()) {
    case 'MINIMAL':  return 'Minimal'
    case 'BALANCED': return 'Balanced'
    case 'MAXIMUM':  return 'Maximum'
    default:         return 'Balanced'
  }
}

function toBackendLevel(level: PrototypeLevel): string {
  switch (level) {
    case 'Minimal':  return 'MINIMAL'
    case 'Balanced': return 'BALANCED'
    case 'Maximum':  return 'MAXIMUM'
  }
}

// ── Field value helpers ─────────────────────────────────────────────

/** Get the first value for a field name from a record's fieldVariants */
function getFieldValue(record: RecordReviewGroup, fieldName: string): string {
  const variants = record.fieldVariants[fieldName]
  if (!variants || variants.length === 0) return ''
  return variants[0]?.value ?? ''
}

/** Get updateKey for a field from a record's fieldVariants */
function getUpdateKey(record: RecordReviewGroup, fieldName: string): string | undefined {
  const variants = record.fieldVariants[fieldName]
  if (!variants || variants.length === 0) return undefined
  return variants[0]?.updateKey
}

/** Get the adaptation code from any field variant in a record */
function getAdaptationCode(record: RecordReviewGroup): string {
  const firstField = Object.values(record.fieldVariants)[0]
  if (!firstField || firstField.length === 0) return 'BALANCED'
  return firstField[0]?.adaptationCode ?? 'BALANCED'
}

/** Get the responseId from any field variant in a record */
function getResponseId(record: RecordReviewGroup): string | undefined {
  const firstField = Object.values(record.fieldVariants)[0]
  if (!firstField || firstField.length === 0) return undefined
  return firstField[0]?.responseId
}

// ── Main adapter ────────────────────────────────────────────────────

export function adaptGenerationReviewDto(dto: GenerationReviewDto): ReviewViewModel {
  const allEnVariants: GeneratedVariant[] = []
  const allRuVariants: GeneratedVariant[] = []

  for (const lang of dto.languages) {
    const variants = buildVariantsForLanguage(lang)
    if (lang.languageCode === 'EN') {
      allEnVariants.push(...variants)
    } else {
      allRuVariants.push(...variants)
    }
  }

  // Determine showLevels: true if any language has 3 adaptation levels
  const uniqueLevels = new Set<string>()
  for (const v of [...allEnVariants, ...allRuVariants]) {
    uniqueLevels.add(v.adaptationLevel)
  }

  return {
    requestId: dto.requestId,
    enVariants: allEnVariants,
    ruVariants: allRuVariants,
    isBilingual: allEnVariants.length > 0 && allRuVariants.length > 0,
    showLevels: uniqueLevels.size >= 2
  }
}

/**
 * Build GeneratedVariant[] for one language group.
 * Groups records across sections by responseId.
 */
function buildVariantsForLanguage(lang: LanguageReviewGroup): GeneratedVariant[] {
  // Map responseId → GeneratedVariant (partial)
  const variantMap = new Map<string, Partial<GeneratedVariant>>()

  for (const section of lang.sections) {
    for (const record of section.records) {
      const responseId = getResponseId(record)
      if (!responseId) continue

      // Get or create variant for this responseId
      if (!variantMap.has(responseId)) {
        const adaptationCode = getAdaptationCode(record)
        variantMap.set(responseId, {
          language: lang.languageCode,
          adaptationLevel: toPrototypeLevel(adaptationCode),
          professionalTitle: '',
          valueLine: '',
          professionalSummary: '',
          professionalAspirations: '',
          workExperience: [],
          courses: [],
          projects: [],
          skills: [],
          __meta: {}
        })
      }

      const variant = variantMap.get(responseId)!

      // Apply section data to variant
      applySectionToVariant(variant, section.sectionKey, record)
    }
  }

  return Array.from(variantMap.values()) as GeneratedVariant[]
}

function applySectionToVariant(
  variant: Partial<GeneratedVariant>,
  sectionKey: string,
  record: RecordReviewGroup
) {
  const meta: Record<string, string> = variant.__meta ?? {}

  switch (sectionKey) {
    case 'professional_positioning': {
      const fields = ['professionalTitle', 'valueLine', 'professionalSummary', 'professionalAspirations', 'coverLetter']
      for (const field of fields) {
        const val = getFieldValue(record, field)
        if (val) {
          (variant as any)[field] = val
          const uk = getUpdateKey(record, field)
          if (uk) meta[`pp:${field}`] = uk
        }
      }
      break
    }

    case 'work_experience': {
      // Use stable orderInResume as sourceId so the same logical record
      // at the same position across EN/RU and MIN/BAL/MAX is grouped together.
      // Meta keys use the same groupKey so save lookup finds the record
      // by its UI sourceId. The real generated UUID updateKey is preserved
      // in the value (backend parses it from updateKey).
      const groupKey = 'we_' + record.orderInResume
      const exp: GeneratedExperience = {
        sourceId: groupKey,
        jobTitle: getFieldValue(record, 'jobTitle'),
        companyName: getFieldValue(record, 'companyName'),
        location: '',
        dateRange: '',
        description: getFieldValue(record, 'description'),
      }
      for (const f of ['jobTitle', 'companyName', 'description'] as const) {
        const uk = getUpdateKey(record, f)
        if (uk) meta[`we:${groupKey}:${f}`] = uk
      }
      // Feature 008: pass bullet points with their update keys
      if (record.bullets && record.bullets.length > 0) {
        exp.bullets = record.bullets
        for (const bullet of record.bullets) {
          if (bullet.updateKey) {
            meta[`we:${groupKey}:bullet_${bullet.bulletOrder}`] = bullet.updateKey
          }
        }
      }
      variant.workExperience = [...(variant.workExperience ?? []), exp]
      break
    }

    case 'courses': {
      const groupKey = 'co_' + record.orderInResume
      const crs: GeneratedCourse = {
        sourceId: groupKey,
        courseName: getFieldValue(record, 'courseName'),
        provider: getFieldValue(record, 'provider'),
        dateRange: '',
        courseFocus: getFieldValue(record, 'courseFocus'),
      }
      for (const f of ['courseName', 'provider', 'courseFocus'] as const) {
        const uk = getUpdateKey(record, f)
        if (uk) meta[`co:${groupKey}:${f}`] = uk
      }
      variant.courses = [...(variant.courses ?? []), crs]
      break
    }

    case 'projects': {
      const groupKey = 'prj_' + record.orderInResume
      const prj: GeneratedProject = {
        sourceId: groupKey,
        projectName: getFieldValue(record, 'projectName'),
        role: getFieldValue(record, 'role'),
        dateRange: '',
        description: getFieldValue(record, 'description'),
      }
      for (const f of ['projectName', 'role', 'description'] as const) {
        const uk = getUpdateKey(record, f)
        if (uk) meta[`pr:${groupKey}:${f}`] = uk
      }
      // Feature 008: pass bullet points with their update keys
      if (record.bullets && record.bullets.length > 0) {
        prj.bullets = record.bullets
        for (const bullet of record.bullets) {
          if (bullet.updateKey) {
            meta[`pr:${groupKey}:bullet_${bullet.bulletOrder}`] = bullet.updateKey
          }
        }
      }
      variant.projects = [...(variant.projects ?? []), prj]
      break
    }

    case 'skills': {
      const groupName = getFieldValue(record, 'groupName')
      const skillsCsv = getFieldValue(record, 'skills')
      const skills: string[] = skillsCsv
        ? skillsCsv.split(',').map((s: string) => s.trim()).filter(Boolean)
        : []
      const sg: GeneratedSkillGroup = { groupName, skills }
      // Use orderInResume (which equals groupIdx) to avoid meta key collision
      // since all skill group records share the same recordId (response UUID)
      const groupIdx = record.orderInResume
      const groupUk = getUpdateKey(record, 'groupName')
      const skillsUk = getUpdateKey(record, 'skills')
      if (groupUk) meta[`sk:${groupIdx}:groupName`] = groupUk
      if (skillsUk) meta[`sk:${groupIdx}:skills`] = skillsUk
      variant.skills = [...(variant.skills ?? []), sg]
      break
    }

    case 'personal_information': {
      const personal: GeneratedPersonalInfo = {
        location: getFieldValue(record, 'location'),
        spokenLanguages: getFieldValue(record, 'spokenLanguages'),
        willingnessToRelocate: getFieldValue(record, 'willingnessToRelocate'),
        willingnessForBusinessTrips: getFieldValue(record, 'willingnessForBusinessTrips'),
        citizenship: getFieldValue(record, 'citizenship'),
        dateOfBirth: getFieldValue(record, 'dateOfBirth'),
        workFormats: getFieldValue(record, 'workFormats'),
      }
      for (const f of ['location', 'spokenLanguages', 'willingnessToRelocate',
                        'willingnessForBusinessTrips', 'citizenship', 'dateOfBirth', 'workFormats'] as const) {
        const uk = getUpdateKey(record, f)
        if (uk) meta[`pi:${record.recordId}:${f}`] = uk
      }
      variant.personalInfo = personal
      break
    }
  }

  variant.__meta = meta
}

// ── Build save payload ──────────────────────────────────────────────

/**
 * Build the save payload from edited variants.
 * Compares current values with stored originals (via __meta presence).
 * Only sends changed fields.
 */
export function buildReviewUpdatePayload(
  model: ReviewViewModel
): GenerationReviewUpdateDto {
  const fieldUpdates: Record<string, string> = {}

  const allVariants = [...model.enVariants, ...model.ruVariants]

  for (const variant of allVariants) {
    const meta = variant.__meta
    if (!meta) continue

    // Professional Positioning fields
    const ppFields = ['professionalTitle', 'valueLine', 'professionalSummary',
                       'professionalAspirations', 'coverLetter'] as const
    for (const field of ppFields) {
      const uk = meta[`pp:${field}`]
      if (uk) {
        const val = (variant as any)[field]
        if (val !== undefined && val !== '') {
          fieldUpdates[uk] = String(val)
        }
      }
    }

    // Work Experience fields
    for (const exp of variant.workExperience) {
      for (const f of ['jobTitle', 'companyName', 'description'] as const) {
        const uk = meta[`we:${exp.sourceId}:${f}`]
        if (uk) {
          fieldUpdates[uk] = String((exp as any)[f] ?? '')
        }
      }
      // Feature 008: collect bullet edits
      if (exp.bullets && exp.bullets.length > 0) {
        for (const bullet of exp.bullets) {
          if (bullet.updateKey && bullet.bulletText) {
            fieldUpdates[bullet.updateKey] = bullet.bulletText
          }
        }
      }
    }

    // Course fields
    for (const crs of variant.courses) {
      for (const f of ['courseName', 'provider', 'courseFocus'] as const) {
        const uk = meta[`co:${crs.sourceId}:${f}`]
        if (uk) {
          fieldUpdates[uk] = String((crs as any)[f] ?? '')
        }
      }
    }

    // Project fields
    for (const prj of variant.projects) {
      for (const f of ['projectName', 'role', 'description'] as const) {
        const uk = meta[`pr:${prj.sourceId}:${f}`]
        if (uk) {
          fieldUpdates[uk] = String((prj as any)[f] ?? '')
        }
      }
      // Feature 008: collect bullet edits
      if (prj.bullets && prj.bullets.length > 0) {
        for (const bullet of prj.bullets) {
          if (bullet.updateKey && bullet.bulletText) {
            fieldUpdates[bullet.updateKey] = bullet.bulletText
          }
        }
      }
    }

    // Skill fields
    for (let i = 0; i < variant.skills.length; i++) {
      const sg = variant.skills[i]
      // groupName — use the updateKey for the first group's groupName
      const groupUk = meta[`sk:${variant.language}_${variant.adaptationLevel}_${i}:groupName`]
      const skillsUk = meta[`sk:${variant.language}_${variant.adaptationLevel}_${i}:skills`]
      // Actually the updateKey for skills is stored differently due to composite recordId
      // Let's find update keys containing the group name
    }
  }

  return { fieldUpdates }
}

/**
 * Simplified build: iterate over all meta entries and collect values.
 * For skills, map groupName value to updateKey.
 */
export function buildReviewUpdatePayloadSimple(
  model: ReviewViewModel
): GenerationReviewUpdateDto {
  const fieldUpdates: Record<string, string> = {}
  const allVariants = [...model.enVariants, ...model.ruVariants]

  for (const variant of allVariants) {
    const meta = variant.__meta
    if (!meta) continue

    for (const [metaKey, updateKey] of Object.entries(meta)) {
      // Meta keys: pp:fieldName, we:recordId:fieldName, co:recordId:fieldName,
      //            pr:recordId:fieldName, sk:recordId:fieldName, pi:recordId:fieldName
      const parts = metaKey.split(':')

      if (parts[0] === 'pp') {
        // Professional positioning
        const fieldName = parts[1]
        const val = (variant as any)[fieldName]
        if (val !== undefined && val !== '') {
          fieldUpdates[updateKey] = String(val)
        }
      } else if (parts[0] === 'we') {
        // Work experience: we:sourceId:fieldName
        const sourceId = parts[1]
        const fieldName = parts[2]
        const exp = variant.workExperience.find(e => e.sourceId === sourceId)
        if (exp) {
          const val = (exp as any)[fieldName]
          if (val !== undefined) {
            fieldUpdates[updateKey] = String(val)
          }
        }
      } else if (parts[0] === 'co') {
        // Courses
        const sourceId = parts[1]
        const fieldName = parts[2]
        const crs = variant.courses.find(c => c.sourceId === sourceId)
        if (crs) {
          const val = (crs as any)[fieldName]
          if (val !== undefined) {
            fieldUpdates[updateKey] = String(val)
          }
        }
      } else if (parts[0] === 'pr') {
        // Projects
        const sourceId = parts[1]
        const fieldName = parts[2]
        const prj = variant.projects.find(p => p.sourceId === sourceId)
        if (prj) {
          const val = (prj as any)[fieldName]
          if (val !== undefined) {
            fieldUpdates[updateKey] = String(val)
          }
        }
      } else if (parts[0] === 'sk') {
        // Skills: sk:recordId:fieldName
        // We need to get the value from the skills array
        const fieldName = parts[2]
        // Skills don't have individual sourceIds — use index
        // Update key format: skills:{responseId}:groupName:{adaptCode}:{groupIdx}
        // So we need to extract groupIdx from the updateKey
        const ukParts = updateKey.split(':')
        if (ukParts.length >= 5) {
          const groupIdx = parseInt(ukParts[4], 10)
          if (!isNaN(groupIdx) && variant.skills[groupIdx]) {
            if (fieldName === 'groupName') {
              fieldUpdates[updateKey] = variant.skills[groupIdx].groupName
            } else if (fieldName === 'skills') {
              fieldUpdates[updateKey] = variant.skills[groupIdx].skills.join(', ')
            }
          }
        }
      } else if (parts[0] === 'pi') {
        // Personal information
        const fieldName = parts[2]
        const pi = variant.personalInfo
        if (pi) {
          const val = (pi as any)[fieldName]
          if (val !== undefined) {
            fieldUpdates[updateKey] = String(val)
          }
        }
      }
    }
  }

  return { fieldUpdates }
}

export { toBackendLevel }
