// =============================================================================
// PROTOTYPE MOCK ONLY — REPLACE WITH REAL JAVA BACKEND API CALL IN PRODUCTION.
// THIS MODULE SIMULATES AI GENERATION AND PDF CONVERSION FOR UX REVIEW ONLY.
// =============================================================================

import type {
  GenerateResumeFormState,
  GeneratedVariant,
  GeneratedPersonalInfo,
  AdaptationLevel,
  ResumeLanguage,
  ResumeLanguageMode
} from '@/types/generate'

// ─── Mock generated data ─────────────────────────────────────────────────────
// PROTOTYPE MOCK ONLY — THESE ARE STATIC EXAMPLES. REPLACE WITH BACKEND RESPONSE DATA.

const MOCK_PROFESSIONAL_TITLES: Record<string, Record<string, string>> = {
  'Minimal': {
    EN: 'Business Analyst',
    RU: 'Бизнес-аналитик'
  },
  'Balanced': {
    EN: 'Experienced Business Analyst | Requirements & Process Optimization',
    RU: 'Опытный бизнес-аналитик | Требования и оптимизация процессов'
  },
  'Maximum': {
    EN: 'Senior Business Analyst | Driving Data-Driven Decisions & Cross-Functional Alignment',
    RU: 'Ведущий бизнес-аналитик | Принятие решений на основе данных и межфункциональная координация'
  }
}

const MOCK_VALUE_LINES: Record<string, Record<string, string>> = {
  'Minimal': {
    EN: 'Business analyst with experience in requirements gathering and process improvement.',
    RU: 'Бизнес-аналитик с опытом сбора требований и улучшения процессов.'
  },
  'Balanced': {
    EN: 'Results-oriented Business Analyst with 4+ years bridging stakeholder needs and technical delivery.',
    RU: 'Ориентированный на результат бизнес-аналитик с опытом 4+ лет на стыке бизнеса и разработки.'
  },
  'Maximum': {
    EN: 'Strategic Business Analyst with a proven track record of aligning product roadmaps with business goals, driving stakeholder consensus, and delivering measurable process improvements across cross-functional teams.',
    RU: 'Стратегический бизнес-аналитик с подтверждённым опытом согласования дорожной карты продуктов с бизнес-целями, достижения консенсуса заинтересованных сторон и реализации измеримых улучшений процессов в кросс-функциональных командах.'
  }
}

const MOCK_SUMMARIES: Record<string, Record<string, string>> = {
  'Minimal': {
    EN: 'Business analyst skilled in requirements gathering, stakeholder communication, and process modeling.',
    RU: 'Бизнес-аналитик с навыками сбора требований, коммуникации с заинтересованными сторонами и моделирования процессов.'
  },
  'Balanced': {
    EN: 'Detail-oriented Business Analyst with 4+ years of experience in FinTech and consulting. Proficient in writing user stories, conducting stakeholder interviews, creating UML/BPMN diagrams, and supporting QA throughout the delivery cycle.',
    RU: 'Внимательный к деталям бизнес-аналитик с опытом 4+ лет в FinTech и консалтинге. Уверенно пишу user stories, провожу интервью со стейкхолдерами, создаю UML/BPMN диаграммы и сопровождаю QA на всех этапах разработки.'
  },
  'Maximum': {
    EN: 'Accomplished Business Analyst with 5+ years of experience driving product discovery, feature definition, and cross-team alignment in FinTech and enterprise environments. Skilled at translating ambiguous business needs into clear technical requirements, facilitating workshops with senior stakeholders, and ensuring delivered solutions meet success criteria through structured validation.',
    RU: 'Опытный бизнес-аналитик с 5+ годами работы в FinTech и корпоративной среде. Умею превращать неоднозначные бизнес-потребности в чёткие технические требования, проводить воркшопы с руководством и обеспечивать соответствие результатов критериям успеха через структурированную валидацию.'
  }
}

const MOCK_ASPIRATIONS: Record<string, Record<string, string>> = {
  'Minimal': {
    EN: 'Growing into a senior product analyst role.',
    RU: 'Развитие в роли старшего продукт-аналитика.'
  },
  'Balanced': {
    EN: 'I aim to grow into a Senior Business Analyst role, deepening my expertise in data-driven product decisions and stakeholder management.',
    RU: 'Стремлюсь развиваться в роли старшего бизнес-аналитика, углубляя навыки в продуктовой аналитике и управлении заинтересованными сторонами.'
  },
  'Maximum': {
    EN: 'I am working toward becoming a Lead Business Analyst who shapes product strategy through deep domain research, quantitative analysis, and cross-functional leadership. In the long term, I aspire to transition into a Product Management role where I can own end-to-end product outcomes.',
    RU: 'Стремлюсь стать ведущим бизнес-аналитиком, формирующим продуктовую стратегию через глубокий доменный анализ, количественные данные и межфункциональное лидерство. В долгосрочной перспективе планирую перейти в управление продуктами.'
  }
}

const MOCK_WORK_EXPERIENCE_EN: Array<{
  sourceId: string
  jobTitle: Record<string, string>
  companyName: Record<string, string>
  location: string
  dateRange: string
  description: Record<string, string>
  bullets: Record<string, string[]>
}> = [
  {
    sourceId: 'exp-1',
    jobTitle: { Minimal: 'Business Analyst', Balanced: 'Business Analyst', Maximum: 'Senior Business Analyst' },
    companyName: { Minimal: 'Robosoft', Balanced: 'Robosoft Digital Solutions', Maximum: 'Robosoft Digital Solutions' },
    location: 'Kazakhstan, Astana',
    dateRange: '2022 – Present',
    description: {
      Minimal: 'Requirements gathering and stakeholder collaboration.',
      Balanced: 'Gathered and documented requirements for 10+ product features. Facilitated grooming sessions and supported QA.',
      Maximum: 'Led requirements discovery for 15+ features across 3 product streams. Facilitated stakeholder workshops, authored BRDs and user stories, managed backlog prioritisation, and coordinated UAT with business users.'
    },
    bullets: {
      Minimal: ['Wrote user stories', 'Conducted stakeholder interviews'],
      Balanced: ['Authored 50+ user stories and acceptance criteria', 'Conducted 20+ stakeholder interviews for new features', 'Created BPMN process models for 3 core domains'],
      Maximum: [
        'Led requirements discovery for 15+ features across 3 product streams',
        'Facilitated stakeholder workshops with C-level executives and domain experts',
        'Authored 12 BRDs and 80+ user stories with acceptance criteria',
        'Managed backlog prioritisation using MoSCoW and WSJF frameworks',
        'Coordinated UAT sessions with 30+ business users across 2 regions'
      ]
    }
  },
  {
    sourceId: 'exp-2',
    jobTitle: { Minimal: 'Operations Assistant', Balanced: 'Operations Assistant', Maximum: 'Operations Assistant' },
    companyName: { Minimal: 'KazInd', Balanced: 'Kazakhstan Industrial Services', Maximum: 'Kazakhstan Industrial Services LLP' },
    location: 'Kazakhstan, Astana',
    dateRange: '2020 – 2022',
    description: {
      Minimal: 'Supported operational reporting and data entry.',
      Balanced: 'Maintained operational dashboards, prepared weekly reports, and supported process automation initiatives.',
      Maximum: 'Maintained operational KPIs dashboards in Excel and Power BI, prepared weekly and monthly reports for management, supported digitalisation initiatives, and automated routine reporting tasks reducing manual effort by 30%.'
    },
    bullets: {
      Minimal: ['Prepared weekly reports', 'Maintained operational data'],
      Balanced: ['Prepared weekly operational reports for management review', 'Maintained KPI dashboards in Excel', 'Supported process automation initiatives'],
      Maximum: [
        'Maintained operational KPI dashboards in Excel and Power BI',
        'Prepared weekly and monthly reports for senior management',
        'Supported digitalisation initiatives across 3 departments',
        'Automated routine reporting tasks, reducing manual effort by 30%'
      ]
    }
  }
]

const MOCK_EDUCATION_EN: Array<{
  sourceId: string
  institutionName: Record<string, string>
  degree: Record<string, string>
  fieldOfStudy: Record<string, string>
  dateRange: string
  description: Record<string, string>
}> = [
  {
    sourceId: 'edu-1',
    institutionName: { Minimal: 'ENU', Balanced: 'L.N. Gumilyov Eurasian National University', Maximum: 'L.N. Gumilyov Eurasian National University' },
    degree: { Minimal: 'Bachelor', Balanced: "Bachelor's Degree", Maximum: "Bachelor's Degree with Honours" },
    fieldOfStudy: { Minimal: 'IT', Balanced: 'Information Technology', Maximum: 'Information Technology' },
    dateRange: '2016 – 2020',
    description: {
      Minimal: 'Graduated with a degree in IT.',
      Balanced: 'Graduated with a Bachelor in Information Technology. Coursework included system analysis and database design.',
      Maximum: 'Graduated with Honours. Relevant coursework: System Analysis and Design, Database Management, Business Process Modelling, Project Management.'
    }
  }
]

const MOCK_COURSES_EN: Array<{
  sourceId: string
  courseName: Record<string, string>
  provider: Record<string, string>
  dateRange: string
  courseFocus: Record<string, string>
}> = [
  {
    sourceId: 'crs-1',
    courseName: { Minimal: 'BA Fundamentals', Balanced: 'Business Analysis Fundamentals', Maximum: 'Business Analysis Fundamentals (IIBA Endorsed)' },
    provider: { Minimal: 'Udemy', Balanced: 'Udemy', Maximum: 'Udemy' },
    dateRange: '2021',
    courseFocus: {
      Minimal: 'Requirements gathering techniques.',
      Balanced: 'Stakeholder analysis, requirements gathering, and UML diagramming.',
      Maximum: 'BABOK v3, stakeholder analysis, requirements lifecycle, UML and BPMN notation, and solution evaluation.'
    }
  }
]

const MOCK_PROJECTS_EN: Array<{
  sourceId: string
  projectName: Record<string, string>
  role: Record<string, string>
  dateRange: string
  description: Record<string, string>
  bullets: Record<string, string[]>
}> = [
  {
    sourceId: 'prj-1',
    projectName: { Minimal: 'CRM Migration', Balanced: 'CRM System Migration', Maximum: 'Enterprise CRM System Migration' },
    role: { Minimal: 'BA', Balanced: 'Business Analyst', Maximum: 'Lead Business Analyst' },
    dateRange: '2023',
    description: {
      Minimal: 'Supported migration of CRM data.',
      Balanced: 'Elicited migration requirements, mapped data fields, and coordinated UAT for CRM migration project.',
      Maximum: 'Elicited and documented migration requirements for 200+ data fields, coordinated data mapping workshops with 5 departments, wrote UAT test cases, and managed defect triage during cutover.'
    },
    bullets: {
      Minimal: ['Elicited migration requirements', 'Coordinated UAT testing'],
      Balanced: ['Elicited migration requirements from 5 departments', 'Mapped 200+ data fields between legacy and new CRM', 'Coordinated UAT testing with 15 business users'],
      Maximum: [
        'Elicited migration requirements from 5 departments through workshops and interviews',
        'Mapped 200+ data fields between legacy CRM and Salesforce',
        'Authored UAT test cases and coordinated testing with 15 business users',
        'Managed defect triage during go-live cutover weekend'
      ]
    }
  }
]

const MOCK_SKILLS_EN: Array<{
  groupName: Record<string, string>
  skills: Record<string, string[]>
}> = [
  {
    groupName: { Minimal: 'Core', Balanced: 'Core BA Skills', Maximum: 'Core Business Analysis Competencies' },
    skills: {
      Minimal: ['Requirements', 'Stakeholder management', 'Process modeling'],
      Balanced: ['Requirements elicitation & documentation', 'Stakeholder management', 'BPMN / UML', 'User stories & acceptance criteria', 'SDLC'],
      Maximum: ['Requirements elicitation & documentation (BRD, FRD, user stories)', 'Stakeholder analysis & management (RACI)',
        'BPMN 2.0 / UML', 'Workshop facilitation', 'Backlog management (MoSCoW, WSJF)', 'UAT coordination', 'SQL (basic)', 'JIRA / Confluence', 'Figma (basic)']
    }
  }
]

function getRussianWorkExperience(): typeof MOCK_WORK_EXPERIENCE_EN {
  return MOCK_WORK_EXPERIENCE_EN.map(exp => ({
    ...exp,
    jobTitle: {
      Minimal: 'Бизнес-аналитик',
      Balanced: 'Бизнес-аналитик',
      Maximum: 'Ведущий бизнес-аналитик'
    },
    description: {
      Minimal: 'Сбор требований и взаимодействие с заинтересованными сторонами.',
      Balanced: 'Собрал и задокументировал требования для 10+ функций продукта. Проводил grooming-сессии и поддерживал QA.',
      Maximum: 'Руководил выявлением требований для 15+ функций в 3 продуктовых потоках. Проводил воркшопы со стейкхолдерами, составлял BRD и user stories, управлял приоритезацией бэклога и координировал UAT с бизнес-пользователями.'
    },
    bullets: {
      Minimal: ['Писал пользовательские истории', 'Проводил интервью со стейкхолдерами'],
      Balanced: ['Составил 50+ user stories и критериев приёмки', 'Провёл 20+ интервью со стейкхолдерами', 'Создал BPMN-модели для 3 доменов'],
      Maximum: [
        'Руководил выявлением требований для 15+ функций в 3 продуктовых потоках',
        'Проводил воркшопы с руководством и экспертами доменов',
        'Составил 12 BRD и 80+ user stories с критериями приёмки',
        'Управлял приоритезацией бэклога с использованием MoSCoW и WSJF',
        'Координировал UAT-сессии с 30+ бизнес-пользователями'
      ]
    }
  }))
}

function getRussianEducation() {
  return MOCK_EDUCATION_EN.map(edu => ({
    ...edu,
    institutionName: { Minimal: 'ЕНУ', Balanced: 'Евразийский национальный университет им. Л.Н. Гумилёва', Maximum: 'Евразийский национальный университет им. Л.Н. Гумилёва' },
    degree: { Minimal: 'Бакалавр', Balanced: 'Бакалавр', Maximum: 'Бакалавр с отличием' },
    fieldOfStudy: { Minimal: 'ИТ', Balanced: 'Информационные технологии', Maximum: 'Информационные технологии' },
    description: {
      Minimal: 'Окончил университет по специальности ИТ.',
      Balanced: 'Окончил бакалавриат по информационным технологиям. Курсы включали системный анализ и проектирование баз данных.',
      Maximum: 'Окончил с отличием. Релевантные курсы: системный анализ и проектирование, управление базами данных, моделирование бизнес-процессов, управление проектами.'
    }
  }))
}

function getRussianCourses() {
  return MOCK_COURSES_EN.map(crs => ({
    ...crs,
    courseName: { Minimal: 'Основы BA', Balanced: 'Основы бизнес-анализа', Maximum: 'Основы бизнес-анализа (одобрено IIBA)' },
    provider: { Minimal: 'Udemy', Balanced: 'Udemy', Maximum: 'Udemy' },
    courseFocus: {
      Minimal: 'Техники сбора требований.',
      Balanced: 'Анализ стейкхолдеров, сбор требований и UML-диаграммы.',
      Maximum: 'BABOK v3, анализ стейкхолдеров, жизненный цикл требований, нотации UML и BPMN, оценку решений.'
    }
  }))
}

function getRussianProjects() {
  return MOCK_PROJECTS_EN.map(prj => ({
    ...prj,
    jobTitle: { Minimal: 'БА', Balanced: 'Бизнес-аналитик', Maximum: 'Ведущий бизнес-аналитик' },
    projectName: { Minimal: 'Миграция CRM', Balanced: 'Миграция CRM-системы', Maximum: 'Миграция корпоративной CRM-системы' },
    description: {
      Minimal: 'Поддержка миграции данных CRM.',
      Balanced: 'Собрал требования к миграции, сопоставил поля данных, координировал UAT.',
      Maximum: 'Собрал и задокументировал требования к миграции для 200+ полей данных, провёл воркшопы с 5 отделами, написал тест-кейсы UAT, управлял triage дефектов во время запуска.'
    },
    bullets: {
      Minimal: ['Собрал требования к миграции', 'Координировал UAT-тестирование'],
      Balanced: ['Собрал требования к миграции от 5 отделов', 'Сопоставил 200+ полей данных между старой и новой CRM', 'Координировал UAT-тестирование с 15 пользователями'],
      Maximum: [
        'Собрал требования к миграции от 5 отделов через воркшопы и интервью',
        'Сопоставил 200+ полей между legacy CRM и Salesforce',
        'Написал тест-кейсы UAT и координировал тестирование с 15 пользователями',
        'Управлял triage дефектов в период запуска'
      ]
    }
  }))
}

// ─── Personal Info (same across adaptation levels per language) ───────────────
// PROTOTYPE MOCK ONLY — REPLACE WITH REAL generation_response_personal DATA FROM BACKEND.

function getPersonalInfo(useRu: boolean): GeneratedPersonalInfo {
  if (useRu) {
    return {
      location: 'Казахстан',
      spokenLanguages: 'Английский C1, русский родной',
      willingnessToRelocate: 'Готов к переезду',
      willingnessForBusinessTrips: 'Готов к командировкам',
      citizenship: 'Казахстан',
      dateOfBirth: '1993-01-01',
      workFormats: 'Полная занятость, гибрид, удалённо',
      gpaGrade: null,
    }
  }
  return {
    location: 'Kazakhstan',
    spokenLanguages: 'English C1, Russian native',
    willingnessToRelocate: 'Open to relocation',
    willingnessForBusinessTrips: 'Open to business trips',
    citizenship: 'Kazakhstan',
    dateOfBirth: '1993-01-01',
    workFormats: 'Full-time, hybrid, remote',
    gpaGrade: null,
  }
}

function getRussianSkills() {
  return MOCK_SKILLS_EN.map(sk => ({
    ...sk,
    groupName: { Minimal: 'Основное', Balanced: 'Основные навыки BA', Maximum: 'Ключевые компетенции бизнес-анализа' },
    skills: {
      Minimal: ['Требования', 'Управление стейкхолдерами', 'Моделирование процессов'],
      Balanced: ['Выявление и документирование требований', 'Управление стейкхолдерами', 'BPMN / UML', 'User stories и критерии приёмки', 'SDLC'],
      Maximum: ['Выявление и документирование требований (BRD, FRD, user stories)', 'Анализ и управление стейкхолдерами (RACI)',
        'BPMN 2.0 / UML', 'Фасилитация воркшопов', 'Управление бэклогом (MoSCoW, WSJF)', 'Координация UAT', 'SQL (базовый)', 'JIRA / Confluence', 'Figma (базовый)']
    }
  }))
}

function buildVariant(language: ResumeLanguage, level: AdaptationLevel): GeneratedVariant {
  const useRu = language === 'RU'
  const titleMap = MOCK_PROFESSIONAL_TITLES[level]
  const valueMap = MOCK_VALUE_LINES[level]
  const summaryMap = MOCK_SUMMARIES[level]
  const aspirationMap = MOCK_ASPIRATIONS[level]

  const workExp = useRu ? getRussianWorkExperience() : MOCK_WORK_EXPERIENCE_EN
  const education = useRu ? getRussianEducation() : MOCK_EDUCATION_EN
  const courses = useRu ? getRussianCourses() : MOCK_COURSES_EN
  const projects = useRu ? getRussianProjects() : MOCK_PROJECTS_EN
  const skillGroups = useRu ? getRussianSkills() : MOCK_SKILLS_EN

  return {
    language,
    adaptationLevel: level,
    personalInfo: getPersonalInfo(useRu),
    professionalTitle: titleMap[language],
    valueLine: valueMap[language],
    professionalSummary: summaryMap[language],
    professionalAspirations: aspirationMap[language],
    workExperience: workExp.map(exp => ({
      sourceId: exp.sourceId,
      jobTitle: exp.jobTitle[level],
      companyName: exp.companyName[level],
      location: exp.location,
      dateRange: exp.dateRange,
      description: exp.description[level],
      bullets: exp.bullets[level]
    })),
    education: education.map(edu => ({
      sourceId: edu.sourceId,
      institutionName: edu.institutionName[level],
      degree: edu.degree[level],
      fieldOfStudy: edu.fieldOfStudy[level],
      dateRange: edu.dateRange,
      description: edu.description[level]
    })),
    courses: courses.map(crs => ({
      sourceId: crs.sourceId,
      courseName: crs.courseName[level],
      provider: crs.provider[level],
      dateRange: crs.dateRange,
      courseFocus: crs.courseFocus[level]
    })),
    projects: projects.map(prj => ({
      sourceId: prj.sourceId,
      projectName: prj.projectName[level],
      role: prj.role[level],
      dateRange: prj.dateRange,
      description: prj.description[level],
      bullets: prj.bullets[level]
    })),
    skills: skillGroups.map(sk => ({
      groupName: sk.groupName[level],
      skills: sk.skills[level]
    })),
    coverLetter: useRu
      ? 'Уважаемая команда найма!\n\nЯ пишу, чтобы выразить свою заинтересованность в позиции, опубликованной вашей компанией. Имея опыт работы бизнес-аналитиком и глубокое понимание процессов, я уверен, что смогу внести ценный вклад в вашу команду.\n\nЗа время моей карьеры я успешно руководил выявлением требований, проводил воркшопы с заинтересованными сторонами и координировал UAT. Я горю желанием применить свои навыки в вашей компании.\n\nС уважением,\nКандидат'
      : 'Dear Hiring Team,\n\nI am writing to express my interest in the position advertised by your company. With a background in business analysis and a strong understanding of process optimisation, I am confident I can bring value to your team.\n\nThroughout my career, I have led requirements discovery, facilitated stakeholder workshops, and coordinated UAT. I am excited about the opportunity to apply my skills at your company.\n\nBest regards,\nCandidate'
  }
}

function getLanguages(form: GenerateResumeFormState): ResumeLanguage[] {
  switch (form.languageMode) {
    case 'English only': return ['EN']
    case 'Russian only': return ['RU']
    case 'Bilingual': return ['EN', 'RU']
  }
}

function getLevels(form: GenerateResumeFormState): AdaptationLevel[] {
  if (form.adaptationSelection === 'All') {
    return ['Minimal', 'Balanced', 'Maximum']
  }
  return [form.adaptationSelection as AdaptationLevel]
}

// ─── Mock generation ─────────────────────────────────────────────────────────
// PROTOTYPE MOCK ONLY — REPLACE WITH REAL AI GENERATION API CALL IN PRODUCTION.
// THIS 15-SECOND DELAY SIMULATES AI GENERATION FOR UX REVIEW ONLY.

export async function mockGenerateResume(form: GenerateResumeFormState): Promise<GeneratedVariant[]> {
  // PROTOTYPE MOCK ONLY — DO NOT KEEP THIS TIMEOUT IN PRODUCTION.
  await delay(15000)

  const languages = getLanguages(form)
  const levels = getLevels(form)
  const variants: GeneratedVariant[] = []

  for (const lang of languages) {
    for (const level of levels) {
      variants.push(buildVariant(lang, level))
    }
  }

  return variants
}

// ─── Mock PDF save ───────────────────────────────────────────────────────────
// PROTOTYPE MOCK ONLY — REPLACE WITH REAL HTML-TO-PDF GENERATION AND SAVED_RESUME API CALL IN PRODUCTION.
// THIS 15-SECOND DELAY SIMULATES PDF CONVERSION FOR UX REVIEW ONLY.

// PROTOTYPE NOTE — BACKEND STORAGE CONVENTION:
//   generated_results/{username}/{public_code}/
//     resume.html
//     resume.pdf
// For bilingual generation, each language has its own public code and folder.
// Example:
//   generated_results/johndoe/L6WYY/resume_en.html
//   generated_results/johndoe/L6WYY/resume_en.pdf
//   generated_results/johndoe/AB12C/resume_ru.html
//   generated_results/johndoe/AB12C/resume_ru.pdf

export async function mockSaveToPdf(): Promise<{ enPublicLink: string; ruPublicLink?: string }> {
  // PROTOTYPE MOCK ONLY — DO NOT KEEP THIS TIMEOUT IN PRODUCTION.
  await delay(15000)

  const code = Math.random().toString(36).substring(2, 7).toUpperCase()

  // PROTOTYPE MOCK ONLY — REPLACE WITH REAL BACKEND-GENERATED PUBLIC PDF URL.
  return {
    enPublicLink: `https://resumainer.com/candidate/${code}/resume-en`,
    ruPublicLink: `https://resumainer.com/candidate/${code}/resume-ru`
  }
}

// ─── Utility ─────────────────────────────────────────────────────────────────

function delay(ms: number): Promise<void> {
  return new Promise(resolve => setTimeout(resolve, ms))
}
