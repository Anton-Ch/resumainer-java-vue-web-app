<template>
  <div class="review-form">
    <Tabs v-model:value="activeTabModel" class="review-tabs">
      <TabList>
        <Tab v-for="tab in tabs" :key="tab.value" :value="tab.value">
          {{ t(tab.labelKey) }}
        </Tab>
      </TabList>

      <TabPanels>
        <!-- ═══════════════════════════════════════════════════
             TAB 0: PROFESSIONAL POSITIONING
             ═══════════════════════════════════════════════════ -->
        <TabPanel value="positioning">
          <div class="tab-content">
            <div v-if="!hasVariants" class="vue-card" style="text-align:center;padding:48px;">
              <p>{{ t('generate.review.noContent') }}</p>
            </div>
            <template v-if="hasVariants">
              <h3 class="vue-h3 section-heading">{{ t('generate.review.tabs.positioning') }}</h3>
              <div class="review-language-grid" :class="{ 'single-language': visibleLanguages.length <= 1 }">
                <template v-for="lang in visibleLanguages" :key="lang">
                  <section class="review-language-card">
                    <div v-if="visibleLanguages.length > 1" class="lang-card-header">
                      <span class="vue-chip" :class="lang === 'EN' ? 'chip-en' : 'chip-ru'">{{ lang }}</span>
                      <span class="lang-card-title">{{ languageCardTitle(lang) }}</span>
                    </div>
                    <template v-for="field in positioningFields" :key="field.key">
                      <div v-if="field.key !== 'coverLetter' || hasCoverLetter" class="vue-card field-card">
                        <label class="vue-form-label">{{ t('generate.review.fields.' + field.key) }}</label>
                        <template v-if="showLevels">
                          <div v-for="level in activeLevels" :key="level" class="review-level-variant">
                            <span class="vue-chip chip-level">{{ levelLabel(level) }}</span>
                            <InputText
                              v-if="field.type === 'input'"
                              :modelValue="getSimpleField(lang, field.key, level)"
                              @update:modelValue="setSimpleField(lang, field.key, level, $event)"
                              class="field-input"
                            />
                            <Textarea
                              v-else
                              :modelValue="getSimpleField(lang, field.key, level)"
                              @update:modelValue="setSimpleField(lang, field.key, level, $event)"
                              :rows="field.rows || 3"
                              class="field-input"
                            />
                          </div>
                        </template>
                        <template v-else>
                          <InputText
                            v-if="field.type === 'input'"
                            :modelValue="getSimpleField(lang, field.key, activeLevels[0])"
                            @update:modelValue="setSimpleField(lang, field.key, activeLevels[0], $event)"
                            class="field-input"
                          />
                          <Textarea
                            v-else
                            :modelValue="getSimpleField(lang, field.key, activeLevels[0])"
                            @update:modelValue="setSimpleField(lang, field.key, activeLevels[0], $event)"
                            :rows="field.rows || 3"
                            class="field-input"
                          />
                        </template>
                      </div>
                    </template>
                  </section>
                </template>
              </div>
              <div v-if="hasVariants" class="tab-footer">
                <div class="vue-alert vue-alert-info" style="margin-bottom:16px;">
                  <i class="pi pi-info-circle" style="margin-top:2px;"></i>
                  <span>{{ helpText }}</span>
                </div>
                <div v-if="showLevels && warningText" class="vue-alert vue-alert-warning" style="margin-bottom:16px;">
                  <i class="pi pi-exclamation-triangle" style="margin-top:2px;"></i>
                  <span>{{ warningText }}</span>
                </div>
                <AdaptationLevelRadioGroup
                  v-if="showLevels"
                  v-model="selectedLevelModel"
                  :title="t('generate.review.chooseLevelToSave')"
                  style="margin-bottom:16px;"
                />
                <div class="footer-actions">
                  <div class="footer-nav-left"></div>
                  <div class="footer-nav-center">
                    <Button
                      :label="t('generate.review.saveToPdf')"
                      icon="pi pi-save"
                      class="p-button-success p-button-lg"
                      :loading="props.isFinalizing"
                      :disabled="props.isFinalizing"
                      @click="$emit('save')"
                    />
                  </div>
                  <div class="footer-nav-right">
                    <Button
                      :label="t('generate.review.nextButton.toWork')"
                      icon="pi pi-arrow-right"
                      iconPos="right"
                      class="p-button-outlined"
                      @click="goNextTab"
                    />
                  </div>
                </div>
              </div>
            </template>
          </div>
        </TabPanel>

        <!-- ═══════════════════════════════════════════════════
             TAB 1: WORK EXPERIENCE
             ═══════════════════════════════════════════════════ -->
        <TabPanel value="work">
          <div class="tab-content">
            <div v-if="!hasVariants" class="vue-card" style="text-align:center;padding:48px;">
              <p>{{ t('generate.review.noContent') }}</p>
            </div>
            <template v-if="hasVariants">
              <h3 class="vue-h3 section-heading">{{ t('generate.review.tabs.work') }}</h3>
              <div v-for="recordId in workRecordIds" :key="recordId" class="review-record-block">
                <h4 class="record-group-title">{{ workRecordHeading(recordId) }}</h4>
                <div class="review-language-grid" :class="{ 'single-language': visibleLanguages.length <= 1 }">
                  <template v-for="lang in visibleLanguages" :key="lang">
                    <section class="review-language-card">
                      <div v-if="visibleLanguages.length > 1" class="lang-card-header">
                        <span class="vue-chip" :class="lang === 'EN' ? 'chip-en' : 'chip-ru'">{{ lang }}</span>
                        <span class="lang-card-title">{{ languageCardTitle(lang) }}</span>
                      </div>
                      <template v-for="field in workFields" :key="field.key">
                        <div class="vue-card field-card">
                          <label class="vue-form-label">{{ t('generate.review.fields.' + field.key) }}</label>
                          <template v-if="showLevels">
                            <div v-for="level in activeLevels" :key="level" class="review-level-variant">
                              <span class="vue-chip chip-level">{{ levelLabel(level) }}</span>
                              <InputText
                                v-if="field.type === 'input'"
                                :modelValue="getWorkField(lang, recordId, field.key, level)"
                                @update:modelValue="setWorkField(lang, recordId, field.key, level, $event)"
                                class="field-input"
                              />
                              <Textarea
                                v-else
                                :modelValue="getWorkField(lang, recordId, field.key, level)"
                                @update:modelValue="setWorkField(lang, recordId, field.key, level, $event)"
                                :rows="field.rows || 3"
                                class="field-input"
                              />
                            </div>
                          </template>
                          <template v-else>
                            <InputText
                              v-if="field.type === 'input'"
                              :modelValue="getWorkField(lang, recordId, field.key, activeLevels[0])"
                              @update:modelValue="setWorkField(lang, recordId, field.key, activeLevels[0], $event)"
                              class="field-input"
                            />
                            <Textarea
                              v-else
                              :modelValue="getWorkField(lang, recordId, field.key, activeLevels[0])"
                              @update:modelValue="setWorkField(lang, recordId, field.key, activeLevels[0], $event)"
                              :rows="field.rows || 3"
                              class="field-input"
                            />
                          </template>
                        </div>
                      </template>
                    </section>
                  </template>
                </div>
                <!-- Feature 008: bullet editing — render bullets after fields -->
                <div v-if="getWorkBullets(recordId).length > 0" class="review-bullets-section">
                  <h5 class="bullets-heading">{{ t('generate.review.bullets.heading') }}</h5>
                  <div v-for="(bullet, bi) in getWorkBullets(recordId)" :key="bi" class="vue-card field-card bullet-card">
                    <div class="bullet-header">
                      <span class="bullet-index">{{ t('generate.review.bullets.index', { n: bullet.bulletOrder + 1 }) }}</span>
                      <span v-if="bullet.isEdited" class="vue-chip chip-edited">{{ t('generate.review.bullets.edited') }}</span>
                    </div>
                    <Textarea
                      :modelValue="bullet.bulletText"
                      @update:modelValue="setWorkBullet(recordId, bullet.bulletOrder, $event)"
                      :rows="2"
                      class="field-input bullet-input"
                      :class="{ 'p-invalid': bulletError(recordId, bullet.bulletOrder) }"
                    />
                    <small v-if="bulletError(recordId, bullet.bulletOrder)" class="p-error bullet-error">
                      {{ t('generate.review.bullets.emptyError') }}
                    </small>
                  </div>
                </div>
              </div>
              <div v-if="hasVariants" class="tab-footer">
                <AdaptationLevelRadioGroup
                  v-if="showLevels"
                  v-model="selectedLevelModel"
                  :title="t('generate.review.chooseLevelToSave')"
                  style="margin-bottom:16px;"
                />
                <div class="footer-actions">
                  <div class="footer-nav-left">
                    <Button
                      :label="t('generate.review.previousButton.toPositioning')"
                      icon="pi pi-arrow-left"
                      class="p-button-outlined"
                      @click="goPrevTab"
                    />
                  </div>
                  <div class="footer-nav-center">
                    <Button
                      :label="t('generate.review.saveToPdf')"
                      icon="pi pi-save"
                      class="p-button-success p-button-lg"
                      :loading="props.isFinalizing"
                      :disabled="props.isFinalizing"
                      @click="$emit('save')"
                    />
                  </div>
                  <div class="footer-nav-right">
                    <Button
                      :label="t('generate.review.nextButton.toCourses')"
                      icon="pi pi-arrow-right"
                      iconPos="right"
                      class="p-button-outlined"
                      @click="goNextTab"
                    />
                  </div>
                </div>
              </div>
            </template>
          </div>
        </TabPanel>

        <!-- ═══════════════════════════════════════════════════
             TAB 2: COURSES & CERTIFICATIONS
             ═══════════════════════════════════════════════════ -->
        <TabPanel value="courses">
          <div class="tab-content">
            <div v-if="!hasVariants" class="vue-card" style="text-align:center;padding:48px;">
              <p>{{ t('generate.review.noContent') }}</p>
            </div>
            <template v-if="hasVariants">
              <h3 class="vue-h3 section-heading">{{ t('generate.review.tabs.courses') }}</h3>
              <div v-for="recordId in courseRecordIds" :key="recordId" class="review-record-block">
                <h4 class="record-group-title">{{ courseRecordHeading(recordId) }}</h4>
                <div class="review-language-grid" :class="{ 'single-language': visibleLanguages.length <= 1 }">
                  <template v-for="lang in visibleLanguages" :key="lang">
                    <section class="review-language-card">
                      <div v-if="visibleLanguages.length > 1" class="lang-card-header">
                        <span class="vue-chip" :class="lang === 'EN' ? 'chip-en' : 'chip-ru'">{{ lang }}</span>
                        <span class="lang-card-title">{{ languageCardTitle(lang) }}</span>
                      </div>
                      <template v-for="field in courseFields" :key="field.key">
                        <div class="vue-card field-card">
                          <label class="vue-form-label">{{ t('generate.review.fields.' + field.key) }}</label>
                          <template v-if="showLevels">
                            <div v-for="level in activeLevels" :key="level" class="review-level-variant">
                              <span class="vue-chip chip-level">{{ levelLabel(level) }}</span>
                              <InputText
                                v-if="field.type === 'input'"
                                :modelValue="getCourseField(lang, recordId, field.key, level)"
                                @update:modelValue="setCourseField(lang, recordId, field.key, level, $event)"
                                class="field-input"
                              />
                              <Textarea
                                v-else
                                :modelValue="getCourseField(lang, recordId, field.key, level)"
                                @update:modelValue="setCourseField(lang, recordId, field.key, level, $event)"
                                :rows="field.rows || 2"
                                class="field-input"
                              />
                            </div>
                          </template>
                          <template v-else>
                            <InputText
                              v-if="field.type === 'input'"
                              :modelValue="getCourseField(lang, recordId, field.key, activeLevels[0])"
                              @update:modelValue="setCourseField(lang, recordId, field.key, activeLevels[0], $event)"
                              class="field-input"
                            />
                            <Textarea
                              v-else
                              :modelValue="getCourseField(lang, recordId, field.key, activeLevels[0])"
                              @update:modelValue="setCourseField(lang, recordId, field.key, activeLevels[0], $event)"
                              :rows="field.rows || 2"
                              class="field-input"
                            />
                          </template>
                        </div>
                      </template>
                    </section>
                  </template>
                </div>
              </div>
              <div v-if="hasVariants" class="tab-footer">
                <AdaptationLevelRadioGroup
                  v-if="showLevels"
                  v-model="selectedLevelModel"
                  :title="t('generate.review.chooseLevelToSave')"
                  style="margin-bottom:16px;"
                />
                <div class="footer-actions">
                  <div class="footer-nav-left">
                    <Button
                      :label="t('generate.review.previousButton.toWork')"
                      icon="pi pi-arrow-left"
                      class="p-button-outlined"
                      @click="goPrevTab"
                    />
                  </div>
                  <div class="footer-nav-center">
                    <Button
                      :label="t('generate.review.saveToPdf')"
                      icon="pi pi-save"
                      class="p-button-success p-button-lg"
                      :loading="props.isFinalizing"
                      :disabled="props.isFinalizing"
                      @click="$emit('save')"
                    />
                  </div>
                  <div class="footer-nav-right">
                    <Button
                      :label="t('generate.review.nextButton.toProjects')"
                      icon="pi pi-arrow-right"
                      iconPos="right"
                      class="p-button-outlined"
                      @click="goNextTab"
                    />
                  </div>
                </div>
              </div>
            </template>
          </div>
        </TabPanel>

        <!-- ═══════════════════════════════════════════════════
             TAB 3: PROJECTS & VOLUNTEERING
             ═══════════════════════════════════════════════════ -->
        <TabPanel value="projects">
          <div class="tab-content">
            <div v-if="!hasVariants" class="vue-card" style="text-align:center;padding:48px;">
              <p>{{ t('generate.review.noContent') }}</p>
            </div>
            <template v-if="hasVariants">
              <h3 class="vue-h3 section-heading">{{ t('generate.review.tabs.projects') }}</h3>
              <div v-for="recordId in projRecordIds" :key="recordId" class="review-record-block">
                <h4 class="record-group-title">{{ projectRecordHeading(recordId) }}</h4>
                <div class="review-language-grid" :class="{ 'single-language': visibleLanguages.length <= 1 }">
                  <template v-for="lang in visibleLanguages" :key="lang">
                    <section class="review-language-card">
                      <div v-if="visibleLanguages.length > 1" class="lang-card-header">
                        <span class="vue-chip" :class="lang === 'EN' ? 'chip-en' : 'chip-ru'">{{ lang }}</span>
                        <span class="lang-card-title">{{ languageCardTitle(lang) }}</span>
                      </div>
                      <template v-for="field in projectFields" :key="field.key">
                        <div class="vue-card field-card">
                          <label class="vue-form-label">{{ t('generate.review.fields.' + field.key) }}</label>
                          <template v-if="showLevels">
                            <div v-for="level in activeLevels" :key="level" class="review-level-variant">
                              <span class="vue-chip chip-level">{{ levelLabel(level) }}</span>
                              <InputText
                                v-if="field.type === 'input'"
                                :modelValue="getProjectField(lang, recordId, field.key, level)"
                                @update:modelValue="setProjectField(lang, recordId, field.key, level, $event)"
                                class="field-input"
                              />
                              <Textarea
                                v-else
                                :modelValue="getProjectField(lang, recordId, field.key, level)"
                                @update:modelValue="setProjectField(lang, recordId, field.key, level, $event)"
                                :rows="field.rows || 3"
                                class="field-input"
                              />
                            </div>
                          </template>
                          <template v-else>
                            <InputText
                              v-if="field.type === 'input'"
                              :modelValue="getProjectField(lang, recordId, field.key, activeLevels[0])"
                              @update:modelValue="setProjectField(lang, recordId, field.key, activeLevels[0], $event)"
                              class="field-input"
                            />
                            <Textarea
                              v-else
                              :modelValue="getProjectField(lang, recordId, field.key, activeLevels[0])"
                              @update:modelValue="setProjectField(lang, recordId, field.key, activeLevels[0], $event)"
                              :rows="field.rows || 3"
                              class="field-input"
                            />
                          </template>
                        </div>
                      </template>
                    </section>
                  </template>
                </div>
                <!-- Feature 008: bullet editing for each project record -->
                <div v-if="getProjBullets(recordId).length > 0" class="review-bullets-section">
                  <h5 class="bullets-heading">{{ t('generate.review.bullets.heading') }}</h5>
                  <div v-for="(bullet, bi) in getProjBullets(recordId)" :key="bi" class="vue-card field-card bullet-card">
                    <div class="bullet-header">
                      <span class="bullet-index">{{ t('generate.review.bullets.index', { n: bullet.bulletOrder + 1 }) }}</span>
                      <span v-if="bullet.isEdited" class="vue-chip chip-edited">{{ t('generate.review.bullets.edited') }}</span>
                    </div>
                    <Textarea
                      :modelValue="bullet.bulletText"
                      @update:modelValue="setProjBullet(recordId, bullet.bulletOrder, $event)"
                      :rows="2"
                      class="field-input bullet-input"
                      :class="{ 'p-invalid': bulletError(recordId, bullet.bulletOrder) }"
                    />
                    <small v-if="bulletError(recordId, bullet.bulletOrder)" class="p-error bullet-error">
                      {{ t('generate.review.bullets.emptyError') }}
                    </small>
                  </div>
                </div>
              </div>
              <div v-if="hasVariants" class="tab-footer">
                <AdaptationLevelRadioGroup
                  v-if="showLevels"
                  v-model="selectedLevelModel"
                  :title="t('generate.review.chooseLevelToSave')"
                  style="margin-bottom:16px;"
                />
                <div class="footer-actions">
                  <div class="footer-nav-left">
                    <Button
                      :label="t('generate.review.previousButton.toCourses')"
                      icon="pi pi-arrow-left"
                      class="p-button-outlined"
                      @click="goPrevTab"
                    />
                  </div>
                  <div class="footer-nav-center">
                    <Button
                      :label="t('generate.review.saveToPdf')"
                      icon="pi pi-save"
                      class="p-button-success p-button-lg"
                      :loading="props.isFinalizing"
                      :disabled="props.isFinalizing"
                      @click="$emit('save')"
                    />
                  </div>
                  <div class="footer-nav-right">
                    <Button
                      :label="t('generate.review.nextButton.toSkills')"
                      icon="pi pi-arrow-right"
                      iconPos="right"
                      class="p-button-outlined"
                      @click="goNextTab"
                    />
                  </div>
                </div>
              </div>
            </template>
          </div>
        </TabPanel>

        <!-- ═══════════════════════════════════════════════════
             TAB 4: SKILLS
             ═══════════════════════════════════════════════════ -->
        <TabPanel value="skills">
          <div class="tab-content">
            <div v-if="!hasVariants" class="vue-card" style="text-align:center;padding:48px;">
              <p>{{ t('generate.review.noContent') }}</p>
            </div>
            <template v-if="hasVariants">
              <h3 class="vue-h3 section-heading">{{ t('generate.review.tabs.skills') }}</h3>
              <div class="review-language-grid" :class="{ 'single-language': visibleLanguages.length <= 1 }">
                <template v-for="lang in visibleLanguages" :key="lang">
                  <section class="review-language-card">
                    <div v-if="visibleLanguages.length > 1" class="lang-card-header">
                      <span class="vue-chip" :class="lang === 'EN' ? 'chip-en' : 'chip-ru'">{{ lang }}</span>
                      <span class="lang-card-title">{{ languageCardTitle(lang) }}</span>
                    </div>
                    <div v-for="(group, gIdx) in getSkillGroups(lang, activeLevels[0])" :key="gIdx" class="vue-card field-card field-card-skill">
                      <div class="vue-form-group" style="margin-bottom:8px;">
                        <label class="vue-form-label">{{ t('generate.review.fields.skillGroup') }}</label>
                        <template v-if="showLevels">
                          <div v-for="level in activeLevels" :key="level" class="review-level-variant">
                            <span class="vue-chip chip-level">{{ levelLabel(level) }}</span>
                            <InputText
                              :modelValue="getSkillGroupName(lang, level, gIdx)"
                              @update:modelValue="setSkillGroupName(lang, level, gIdx, $event)"
                              class="field-input"
                            />
                          </div>
                        </template>
                        <template v-else>
                          <InputText
                            :modelValue="getSkillGroupName(lang, activeLevels[0], gIdx)"
                            @update:modelValue="setSkillGroupName(lang, activeLevels[0], gIdx, $event)"
                            class="field-input"
                          />
                        </template>
                      </div>
                      <div class="vue-form-group">
                        <label class="vue-form-label vue-label-sm">{{ t('generate.review.fields.skill') }}</label>
                        <template v-if="showLevels">
                          <div v-for="level in activeLevels" :key="level" class="review-level-variant">
                            <span class="vue-chip chip-level">{{ levelLabel(level) }}</span>
                            <InputText
                              :modelValue="getSkillGroupCsv(lang, level, gIdx)"
                              @update:modelValue="setSkillGroupCsv(lang, level, gIdx, $event)"
                              class="field-input"
                            />
                          </div>
                        </template>
                        <template v-else>
                          <InputText
                            :modelValue="getSkillGroupCsv(lang, activeLevels[0], gIdx)"
                            @update:modelValue="setSkillGroupCsv(lang, activeLevels[0], gIdx, $event)"
                            class="field-input"
                          />
                        </template>
                      </div>
                    </div>
                  </section>
                </template>
              </div>
              <div v-if="hasVariants" class="tab-footer">
                <AdaptationLevelRadioGroup
                  v-if="showLevels"
                  v-model="selectedLevelModel"
                  :title="t('generate.review.chooseLevelToSave')"
                  style="margin-bottom:16px;"
                />
                <div class="footer-actions">
                  <div class="footer-nav-left">
                    <Button
                      :label="t('generate.review.previousButton.toProjects')"
                      icon="pi pi-arrow-left"
                      class="p-button-outlined"
                      @click="goPrevTab"
                    />
                  </div>
                  <div class="footer-nav-center">
                    <Button
                      :label="t('generate.review.saveToPdf')"
                      icon="pi pi-save"
                      class="p-button-success p-button-lg"
                      :loading="props.isFinalizing"
                      :disabled="props.isFinalizing"
                      @click="$emit('save')"
                    />
                  </div>
                  <div class="footer-nav-right">
                    <Button
                      :label="t('generate.review.nextButton.toPersonal')"
                      icon="pi pi-arrow-right"
                      iconPos="right"
                      class="p-button-outlined"
                      @click="goNextTab"
                    />
                  </div>
                </div>
              </div>
            </template>
          </div>
        </TabPanel>

        <!-- ═══════════════════════════════════════════════════
             TAB 5: PERSONAL INFORMATION
             ═══════════════════════════════════════════════════ -->
        <TabPanel value="personal">
          <div class="tab-content">
            <div v-if="!hasVariants" class="vue-card" style="text-align:center;padding:48px;">
              <p>{{ t('generate.review.noContent') }}</p>
            </div>
            <template v-if="hasVariants">
              <h3 class="vue-h3 section-heading">{{ t('generate.review.tabs.personal') }}</h3>
              <div class="review-language-grid" :class="{ 'single-language': visibleLanguages.length <= 1 }">
                <template v-for="lang in visibleLanguages" :key="lang">
                  <section class="review-language-card">
                    <div v-if="visibleLanguages.length > 1" class="lang-card-header">
                      <span class="vue-chip" :class="lang === 'EN' ? 'chip-en' : 'chip-ru'">{{ lang }}</span>
                      <span class="lang-card-title">{{ languageCardTitle(lang) }}</span>
                    </div>
                    <template v-for="field in visiblePersonalFields" :key="field.key">
                      <div class="vue-card field-card">
                        <label class="vue-form-label">{{ t('generate.review.fields.' + field.key) }}</label>
                        <template v-if="showLevels">
                          <div v-for="level in activeLevels" :key="level" class="review-level-variant">
                            <span class="vue-chip chip-level">{{ levelLabel(level) }}</span>
                            <DatePicker
                              v-if="field.type === 'date'"
                              :modelValue="getPersonalDate(lang, level)"
                              @update:modelValue="setPersonalDate(lang, level, $event)"
                              class="field-input" :showIcon="true" :maxDate="maxBirthDate"
                              dateFormat="yy-mm-dd"
                            />
                            <InputText
                              v-else
                              :modelValue="getPersonalField(lang, field.key, level)"
                              @update:modelValue="setPersonalField(lang, field.key, level, $event)"
                              class="field-input"
                            />
                          </div>
                        </template>
                        <template v-else>
                          <DatePicker
                            v-if="field.type === 'date'"
                            :modelValue="getPersonalDate(lang, activeLevels[0])"
                            @update:modelValue="setPersonalDate(lang, activeLevels[0], $event)"
                            class="field-input" :showIcon="true" :maxDate="maxBirthDate"
                            dateFormat="yy-mm-dd"
                          />
                          <InputText
                            v-else
                            :modelValue="getPersonalField(lang, field.key, activeLevels[0])"
                            @update:modelValue="setPersonalField(lang, field.key, activeLevels[0], $event)"
                            class="field-input"
                          />
                        </template>
                      </div>
                    </template>
                  </section>
                </template>
              </div>
              <div v-if="hasVariants" class="tab-footer">
                <AdaptationLevelRadioGroup
                  v-if="showLevels"
                  v-model="selectedLevelModel"
                  :title="t('generate.review.chooseLevelToSave')"
                  style="margin-bottom:16px;"
                />
                <div class="footer-actions">
                  <div class="footer-nav-left">
                    <Button
                      :label="t('generate.review.previousButton.toSkills')"
                      icon="pi pi-arrow-left"
                      class="p-button-outlined"
                      @click="goPrevTab"
                    />
                  </div>
                  <div class="footer-nav-center">
                    <Button
                      :label="t('generate.review.saveToPdf')"
                      icon="pi pi-save"
                      class="p-button-success p-button-lg"
                      :loading="props.isFinalizing"
                      :disabled="props.isFinalizing"
                      @click="$emit('save')"
                    />
                  </div>
                  <div class="footer-nav-right"></div>
                </div>
              </div>
            </template>
          </div>
        </TabPanel>
      </TabPanels>
    </Tabs>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import type {
  GeneratedVariant,
  GeneratedSkillGroup,
  PrototypeLevel,
  BulletReviewItem
} from '@/types/generate'
import Tabs from 'primevue/tabs'
import TabList from 'primevue/tablist'
import Tab from 'primevue/tab'
import TabPanels from 'primevue/tabpanels'
import TabPanel from 'primevue/tabpanel'
import InputText from 'primevue/inputtext'
import DatePicker from 'primevue/datepicker'
import Textarea from 'primevue/textarea'
import Button from 'primevue/button'
import AdaptationLevelRadioGroup from './AdaptationLevelRadioGroup.vue'

const { t } = useI18n()

const props = defineProps<{
  enVariants: GeneratedVariant[]
  ruVariants: GeneratedVariant[]
  isBilingual: boolean
  showLevels: boolean
  activeTab: string
  selectedLevel: PrototypeLevel
  isFinalizing?: boolean
}>()

const emit = defineEmits<{
  (e: 'update:activeTab', val: string): void
  (e: 'update:selectedLevel', val: PrototypeLevel): void
  (e: 'save'): void
}>()

// ── Tab configuration ───────────────────────────────────────────────

const tabs = [
  { value: 'positioning', labelKey: 'generate.review.tabs.positioning' },
  { value: 'work', labelKey: 'generate.review.tabs.work' },
  { value: 'courses', labelKey: 'generate.review.tabs.courses' },
  { value: 'projects', labelKey: 'generate.review.tabs.projects' },
  { value: 'skills', labelKey: 'generate.review.tabs.skills' },
  { value: 'personal', labelKey: 'generate.review.tabs.personal' },
]

const tabOrder = ['positioning', 'work', 'courses', 'projects', 'skills', 'personal']

// ── Two-way binding helpers ─────────────────────────────────────────

const activeTabModel = computed({
  get: () => props.activeTab || 'positioning',
  set: (val: string) => emit('update:activeTab', val),
})

const selectedLevelModel = computed({
  get: () => props.selectedLevel,
  set: (val: PrototypeLevel) => emit('update:selectedLevel', val),
})

// ── Computed data helpers ───────────────────────────────────────────

const hasVariants = computed(() => props.enVariants.length > 0 || props.ruVariants.length > 0)

const visibleLanguages = computed(() => {
  const langs: string[] = []
  if (props.enVariants.length > 0) langs.push('EN')
  if (props.ruVariants.length > 0) langs.push('RU')
  return langs
})

const activeLevels = computed(() => {
  if (props.showLevels) return ['Minimal', 'Balanced', 'Maximum'] as const
  const first = props.enVariants[0] || props.ruVariants[0]
  if (first) return [first.adaptationLevel] as const
  return ['Balanced'] as const
})

const showLevels = computed(() => props.showLevels)

const hasCoverLetter = computed(() => {
  return props.enVariants.some(v => !!v.coverLetter) || props.ruVariants.some(v => !!v.coverLetter)
})

const helpText = computed(() => {
  if (props.showLevels) {
    return t('generate.review.help.chooseLevelSave')
  }
  return t('generate.review.help.simpleSave')
})

const warningText = computed(() => {
  if (props.isBilingual && props.showLevels) {
    return t('generate.review.help.oneLevelWarningBilingual')
  }
  if (props.showLevels) {
    return t('generate.review.help.oneLevelWarning')
  }
  return ''
})

// ── Field definitions ───────────────────────────────────────────────

const input = { type: 'input' as const }
const textarea = (rows: number) => ({ type: 'textarea' as const, rows })
const dateType = { type: 'date' as const }

const positioningFields = [
  { key: 'professionalTitle', ...input },
  { key: 'valueLine', ...textarea(2) },
  { key: 'professionalSummary', ...textarea(4) },
  { key: 'professionalAspirations', ...textarea(3) },
  { key: 'coverLetter', ...textarea(6) },
]

const workFields = [
  { key: 'jobTitle', ...input },
  { key: 'companyName', ...input },
  { key: 'description', ...textarea(3) },
]

const courseFields = [
  { key: 'courseName', ...input },
  { key: 'provider', ...input },
  { key: 'courseFocus', ...textarea(2) },
]

const projectFields = [
  { key: 'projectName', ...input },
  { key: 'role', ...input },
  { key: 'description', ...textarea(3) },
]

const personalFields = [
  { key: 'location', ...input },
  { key: 'spokenLanguages', ...input },
  { key: 'willingnessToRelocate', ...input },
  { key: 'willingnessForBusinessTrips', ...input },
  { key: 'citizenship', ...input },
  { key: 'dateOfBirth', ...dateType },
  { key: 'workFormats', ...input },
]

/**
 * Filters personal fields to only show those with non-empty values
 * in at least one visible variant. This hides optional fields like
 * spokenLanguages when the profile has no data for them.
 * Empty fields are hidden, not auto-filled (BUG-007-PERSONAL-002).
 */
const visiblePersonalFields = computed(() => {
  return personalFields.filter(field => {
    for (const lang of visibleLanguages.value) {
      for (const level of activeLevels.value) {
        const val = getPersonalField(lang, field.key, level)
        if (val && val.trim() !== '') return true
      }
    }
    return false
  })
})

// ── Localization helpers ────────────────────────────────────────────

function levelLabel(level: string): string {
  switch (level) {
    case 'Minimal': return t('generate.review.levels.minimal')
    case 'Balanced': return t('generate.review.levels.balanced')
    case 'Maximum': return t('generate.review.levels.maximum')
    default: return level
  }
}

function languageCardTitle(lang: string): string {
  if (lang === 'EN') return t('generate.review.languageCard.english')
  return t('generate.review.languageCard.russian')
}

// ── Variant helper ──────────────────────────────────────────────────

function findVariant(lang: string, level: string): GeneratedVariant | undefined {
  const source = lang === 'EN' ? props.enVariants : props.ruVariants
  const protoLevel = level as PrototypeLevel
  return source.find(v => v.adaptationLevel === protoLevel)
}

// ── Simple field get/set (positioning) ──────────────────────────────

function getSimpleField(lang: string, fieldKey: string, level: string): string {
  const variant = findVariant(lang, level)
  if (!variant) return ''
  return String((variant as any)[fieldKey] ?? '')
}

function setSimpleField(lang: string, fieldKey: string, level: string, val: any) {
  const variant = findVariant(lang, level)
  if (variant && val !== undefined) {
    (variant as any)[fieldKey] = val
  }
}

// ── Work Experience ─────────────────────────────────────────────────

const workRecordIds = computed(() => {
  const ids = new Set<string>()
  for (const v of [...props.enVariants, ...props.ruVariants]) {
    for (const exp of v.workExperience) {
      ids.add(exp.sourceId)
    }
  }
  return Array.from(ids)
})

function workRecordHeading(sourceId: string): string {
  const preferred = findVariant('EN', 'Balanced') || props.enVariants[0] || props.ruVariants[0]
  if (preferred) {
    const exp = preferred.workExperience.find(e => e.sourceId === sourceId)
    if (exp && (exp.jobTitle || exp.companyName)) {
      return `${exp.jobTitle} — ${exp.companyName}`
    }
  }
  return t('generate.review.tabs.work')
}

function getWorkField(lang: string, sourceId: string, fieldKey: string, level: string): string {
  const variant = findVariant(lang, level)
  if (!variant) return ''
  const record = variant.workExperience.find(e => e.sourceId === sourceId)
  if (!record) return ''
  return String((record as any)[fieldKey] ?? '')
}

function setWorkField(lang: string, sourceId: string, fieldKey: string, level: string, val: any) {
  const variant = findVariant(lang, level)
  if (variant && val !== undefined) {
    const record = variant.workExperience.find(e => e.sourceId === sourceId)
    if (record) {
      (record as any)[fieldKey] = val
    }
  }
}

// ── Courses ─────────────────────────────────────────────────────────

const courseRecordIds = computed(() => {
  const ids = new Set<string>()
  for (const v of [...props.enVariants, ...props.ruVariants]) {
    for (const c of v.courses) {
      ids.add(c.sourceId)
    }
  }
  return Array.from(ids)
})

function courseRecordHeading(sourceId: string): string {
  const preferred = findVariant('EN', 'Balanced') || props.enVariants[0] || props.ruVariants[0]
  if (preferred) {
    const crs = preferred.courses.find(c => c.sourceId === sourceId)
    if (crs && crs.courseName) {
      return crs.courseName
    }
  }
  return t('generate.review.tabs.courses')
}

function getCourseField(lang: string, sourceId: string, fieldKey: string, level: string): string {
  const variant = findVariant(lang, level)
  if (!variant) return ''
  const record = variant.courses.find(c => c.sourceId === sourceId)
  if (!record) return ''
  return String((record as any)[fieldKey] ?? '')
}

function setCourseField(lang: string, sourceId: string, fieldKey: string, level: string, val: any) {
  const variant = findVariant(lang, level)
  if (variant && val !== undefined) {
    const record = variant.courses.find(c => c.sourceId === sourceId)
    if (record) {
      (record as any)[fieldKey] = val
    }
  }
}

// ── Projects ────────────────────────────────────────────────────────

const projRecordIds = computed(() => {
  const ids = new Set<string>()
  for (const v of [...props.enVariants, ...props.ruVariants]) {
    for (const p of v.projects) {
      ids.add(p.sourceId)
    }
  }
  return Array.from(ids)
})

function projectRecordHeading(sourceId: string): string {
  const preferred = findVariant('EN', 'Balanced') || props.enVariants[0] || props.ruVariants[0]
  if (preferred) {
    const prj = preferred.projects.find(p => p.sourceId === sourceId)
    if (prj && prj.projectName) {
      return prj.projectName
    }
  }
  return t('generate.review.tabs.projects')
}

function getProjectField(lang: string, sourceId: string, fieldKey: string, level: string): string {
  const variant = findVariant(lang, level)
  if (!variant) return ''
  const record = variant.projects.find(p => p.sourceId === sourceId)
  if (!record) return ''
  return String((record as any)[fieldKey] ?? '')
}

function setProjectField(lang: string, sourceId: string, fieldKey: string, level: string, val: any) {
  const variant = findVariant(lang, level)
  if (variant && val !== undefined) {
    const record = variant.projects.find(p => p.sourceId === sourceId)
    if (record) {
      (record as any)[fieldKey] = val
    }
  }
}

// Feature 008: bullet editing functions for work experience and projects

function getWorkBullets(sourceId: string): BulletReviewItem[] {
  const allVariants = [...props.enVariants, ...props.ruVariants]
  for (const v of allVariants) {
    const exp = v.workExperience.find(e => e.sourceId === sourceId)
    if (exp && exp.bullets && exp.bullets.length > 0) return exp.bullets
  }
  return []
}

function setWorkBullet(sourceId: string, bulletOrder: number, text: string) {
  const allVariants = [...props.enVariants, ...props.ruVariants]
  for (const v of allVariants) {
    const exp = v.workExperience.find(e => e.sourceId === sourceId)
    if (exp && exp.bullets) {
      const bullet = exp.bullets.find(b => b.bulletOrder === bulletOrder)
      if (bullet) { bullet.bulletText = text; return }
    }
  }
}

function getProjBullets(sourceId: string): BulletReviewItem[] {
  const allVariants = [...props.enVariants, ...props.ruVariants]
  for (const v of allVariants) {
    const prj = v.projects.find(p => p.sourceId === sourceId)
    if (prj && prj.bullets && prj.bullets.length > 0) return prj.bullets
  }
  return []
}

function setProjBullet(sourceId: string, bulletOrder: number, text: string) {
  const allVariants = [...props.enVariants, ...props.ruVariants]
  for (const v of allVariants) {
    const prj = v.projects.find(p => p.sourceId === sourceId)
    if (prj && prj.bullets) {
      const bullet = prj.bullets.find(b => b.bulletOrder === bulletOrder)
      if (bullet) { bullet.bulletText = text; return }
    }
  }
}

// Feature 008: T037 — bullet validation (reject empty/whitespace)
const bulletErrors = reactive(new Map<string, boolean>())

function bulletError(sourceId: string, bulletOrder: number): boolean {
  return bulletErrors.get(sourceId + ':' + bulletOrder) ?? false
}

function validateBullets(): boolean {
  bulletErrors.clear()
  let hasError = false
  const allVariants = [...props.enVariants, ...props.ruVariants]
  for (const v of allVariants) {
    for (const exp of v.workExperience) {
      if (exp.bullets) {
        for (const b of exp.bullets) {
          if (!b.bulletText || b.bulletText.trim() === '') {
            bulletErrors.set(exp.sourceId + ':' + b.bulletOrder, true)
            hasError = true
          }
        }
      }
    }
    for (const prj of v.projects) {
      if (prj.bullets) {
        for (const b of prj.bullets) {
          if (!b.bulletText || b.bulletText.trim() === '') {
            bulletErrors.set(prj.sourceId + ':' + b.bulletOrder, true)
            hasError = true
          }
        }
      }
    }
  }
  return !hasError
}

// Feature 008: T038 — MVP Review UI does NOT add, delete, or reorder bullets.
// Future enhancement: bullet CRUD operations (add, remove, reorder) may be added here
// by extending the GeneratedExperience/GeneratedProject.bullets array and the save payload.

// ── Personal Information ────────────────────────────────────────────

function getPersonalField(lang: string, fieldKey: string, level: string): string {
  const variant = findVariant(lang, level)
  if (!variant || !variant.personalInfo) return ''
  return String((variant.personalInfo as any)[fieldKey] ?? '')
}

function setPersonalField(lang: string, fieldKey: string, level: string, val: any) {
  const variant = findVariant(lang, level)
  if (variant && variant.personalInfo && val !== undefined) {
    (variant.personalInfo as any)[fieldKey] = val
  }
}

// ── Personal Information: Date field (DatePicker) ────────────────────

/** Max allowed birth date: 15 years ago from today */
const maxBirthDate = new Date()
maxBirthDate.setFullYear(maxBirthDate.getFullYear() - 15)

function getPersonalDate(lang: string, level: string): Date | null {
  const variant = findVariant(lang, level)
  if (!variant || !variant.personalInfo) return null
  const str = String((variant.personalInfo as any).dateOfBirth ?? '')
  if (!str || str.trim() === '') return null
  const parsed = new Date(str + 'T00:00:00')
  return isNaN(parsed.getTime()) ? null : parsed
}

function setPersonalDate(lang: string, level: string, val: Date | Date[] | (Date | null)[] | null | undefined) {
  // DatePicker may emit Date[], (Date|null)[], Date, or null — take first valid Date
  const date: Date | null = Array.isArray(val) ? (val[0] ?? null) : (val ?? null)
  const variant = findVariant(lang, level)
  if (variant && variant.personalInfo) {
    if (date && !isNaN(date.getTime())) {
      const yyyy = date.getFullYear()
      const mm = String(date.getMonth() + 1).padStart(2, '0')
      const dd = String(date.getDate()).padStart(2, '0')
      ;(variant.personalInfo as any).dateOfBirth = `${yyyy}-${mm}-${dd}`
    }
  }
}

// ── Skills ──────────────────────────────────────────────────────────

function getSkillGroups(lang: string, level: string): GeneratedSkillGroup[] {
  const variant = findVariant(lang, level)
  return variant?.skills ?? []
}

function getSkillGroupName(lang: string, level: string, groupIdx: number): string {
  const variant = findVariant(lang, level)
  if (!variant || !variant.skills[groupIdx]) return ''
  return variant.skills[groupIdx].groupName
}

function setSkillGroupName(lang: string, level: string, groupIdx: number, val: any) {
  const variant = findVariant(lang, level)
  if (variant && variant.skills[groupIdx] && val !== undefined) {
    variant.skills[groupIdx].groupName = val
  }
}

function getSkillGroupCsv(lang: string, level: string, groupIdx: number): string {
  const variant = findVariant(lang, level)
  if (!variant || !variant.skills[groupIdx]) return ''
  return variant.skills[groupIdx].skills.join(', ')
}

function setSkillGroupCsv(lang: string, level: string, groupIdx: number, val: any) {
  const variant = findVariant(lang, level)
  if (variant && variant.skills[groupIdx] && val !== undefined) {
    variant.skills[groupIdx].skills = String(val).split(',').map(s => s.trim())
  }
}

// ── Navigation ──────────────────────────────────────────────────────

function goPrevTab() {
  const currentIndex = tabOrder.indexOf(activeTabModel.value)
  if (currentIndex > 0) {
    activeTabModel.value = tabOrder[currentIndex - 1]
  }
}

function goNextTab() {
  const currentIndex = tabOrder.indexOf(activeTabModel.value)
  if (currentIndex < tabOrder.length - 1) {
    activeTabModel.value = tabOrder[currentIndex + 1]
  }
}
</script>

<style scoped>
.review-form {
  display: flex;
  flex-direction: column;
}
.review-tabs {
  width: 100%;
}
.tab-content {
  display: flex;
  flex-direction: column;
  gap: 0;
}
.section-heading {
  margin-bottom: 20px;
  color: var(--vue-text-primary);
}
.review-language-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 24px;
  align-items: start;
}
.review-language-grid.single-language {
  grid-template-columns: minmax(0, 1fr);
}
@media (max-width: 768px) {
  .review-language-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
.review-language-card {
  border: 1px solid var(--vue-border-soft);
  border-radius: var(--vue-radius-lg);
  background: var(--vue-bg-surface);
  box-shadow: var(--vue-shadow-card);
  padding: 16px;
}
.lang-card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--vue-border-soft);
}
.lang-card-title {
  font-family: 'Manrope', sans-serif;
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--vue-text-primary);
}
.review-record-block {
  padding: 0 0 28px 0;
}
.review-record-block + .review-record-block {
  border-top: 1px solid var(--vue-border-soft);
  margin-top: 28px;
  padding-top: 28px;
}
.record-group-title {
  font-family: 'Manrope', sans-serif;
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--vue-text-primary);
  margin-bottom: 16px;
}
.field-card {
  margin-bottom: 12px;
}
.field-card:last-child {
  margin-bottom: 0;
}
.field-card-skill {
  margin-bottom: 12px;
}
.field-input {
  width: 100%;
}
.review-level-variant {
  display: grid;
  grid-template-columns: 140px minmax(0, 1fr);
  align-items: start;
  gap: 12px;
  margin-top: 10px;
  width: 100%;
}
.review-level-variant .chip-level {
  justify-self: start;
  max-width: 100%;
  white-space: nowrap;
}
.review-level-variant :deep(.p-inputtext),
.review-level-variant :deep(.p-textarea) {
  width: 100% !important;
  min-width: 0;
  box-sizing: border-box;
}
@media (max-width: 640px) {
  .review-level-variant {
    grid-template-columns: minmax(0, 1fr);
    gap: 6px;
  }
  .review-level-variant .chip-level {
    margin-bottom: 2px;
  }
}
.chip-en {
  background: var(--vue-accent-bg-blue);
  border-color: var(--vue-accent-blue);
  color: var(--vue-accent-blue);
}
.chip-ru {
  background: var(--vue-accent-bg-warning);
  border-color: var(--vue-accent-warning);
  color: var(--vue-accent-warning);
}
.chip-level {
  background: var(--vue-accent-bg-primary);
  border-color: var(--vue-accent-primary);
  color: var(--vue-accent-primary);
}
.tab-footer {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid var(--vue-border-soft);
}
.footer-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.footer-nav-left {
  flex: 1;
  display: flex;
  justify-content: flex-start;
}
.footer-nav-center {
  flex: 0 0 auto;
}
.footer-nav-right {
  flex: 1;
  display: flex;
  justify-content: flex-end;
}
.vue-label-sm {
  font-size: 0.82rem;
  color: var(--vue-text-secondary);
}
</style>
