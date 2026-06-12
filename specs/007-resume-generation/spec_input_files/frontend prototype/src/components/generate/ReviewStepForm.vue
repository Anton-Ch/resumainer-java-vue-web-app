<template>
  <div class="review-form">
    <TabView v-model:activeIndex="activeTab" class="review-tabs">
      <!-- Tab 0: Professional Positioning -->
      <TabPanel :header="$t('generate.review.tabs.positioning')" value="0">
        <div class="tab-content">
          <div v-if="!hasVariants" class="vue-card" style="text-align:center;padding:48px;">
            <p>{{ $t('generate.review.noContent') }}</p>
          </div>
          <template v-if="hasVariants">
            <h3 class="vue-h3 section-heading">{{ $t('generate.review.tabs.positioning') }}</h3>
            <div class="review-language-grid" :class="{ 'single-language': visibleLanguages.length <= 1 }">
              <template v-for="lang in visibleLanguages" :key="lang">
                <section class="review-language-card">
                  <div v-if="visibleLanguages.length > 1" class="lang-card-header">
                    <span class="vue-chip" :class="lang === 'EN' ? 'chip-en' : 'chip-ru'">{{ lang }}</span>
                    <span class="lang-card-title">{{ languageCardTitle(lang) }}</span>
                  </div>
                  <template v-for="field in positioningFields" :key="field.key">
                    <div v-if="field.key !== 'coverLetter' || hasCoverLetter" class="vue-card field-card">
                      <label class="vue-form-label">{{ $t('generate.review.fields.' + field.key) }}</label>
                      <template v-if="showLevelChips">
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
          </template>
          <div v-if="hasVariants" class="tab-footer">
            <div class="vue-alert vue-alert-info" style="margin-bottom:16px;">
              <i class="pi pi-info-circle" style="margin-top:2px;"></i>
              <span>{{ helpText }}</span>
            </div>
            <div v-if="showLevelChips && warningText" class="vue-alert vue-alert-warning" style="margin-bottom:16px;">
              <i class="pi pi-exclamation-triangle" style="margin-top:2px;"></i>
              <span>{{ warningText }}</span>
            </div>
            <AdaptationLevelRadioGroup
              v-if="showLevelChips"
              v-model="selectedLevel"
              :title="$t('generate.review.chooseLevelToSave')"
              style="margin-bottom:16px;"
            />
            <div class="footer-actions">
              <div class="footer-nav-left"></div>
              <div class="footer-nav-center">
                <Button
                  :label="$t('generate.review.saveToPdf')"
                  icon="pi pi-save"
                  class="p-button-success p-button-lg"
                  @click="$emit('save')"
                />
              </div>
              <div class="footer-nav-right">
                <Button
                  :label="$t('generate.review.nextButton.toWork')"
                  icon="pi pi-arrow-right"
                  iconPos="right"
                  class="p-button-outlined"
                  @click="nextTab"
                />
              </div>
            </div>
          </div>
        </div>
      </TabPanel>

      <!-- Tab 1: Work Experience -->
      <TabPanel :header="$t('generate.review.tabs.work')" value="1">
        <div class="tab-content">
          <div v-if="!hasVariants" class="vue-card" style="text-align:center;padding:48px;">
            <p>{{ $t('generate.review.noContent') }}</p>
          </div>
          <template v-if="hasVariants">
            <h3 class="vue-h3 section-heading">{{ $t('generate.review.tabs.work') }}</h3>
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
                      <div v-if="field.key !== '__bullets__'" class="vue-card field-card">
                        <label class="vue-form-label">{{ $t('generate.review.fields.' + field.key) }}</label>
                        <template v-if="showLevelChips">
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
                    <!-- Bullets -->
                    <template v-for="bIdx in workBulletCount(recordId)" :key="'b' + bIdx">
                      <div class="vue-card field-card">
                        <label class="vue-form-label">{{ $t('generate.review.fields.bullet') }} {{ bIdx }}</label>
                        <template v-if="showLevelChips">
                          <div v-for="level in activeLevels" :key="level" class="review-level-variant">
                            <span class="vue-chip chip-level">{{ levelLabel(level) }}</span>
                            <InputText
                              :modelValue="getWorkBullet(lang, recordId, bIdx - 1, level)"
                              @update:modelValue="setWorkBullet(lang, recordId, bIdx - 1, level, $event)"
                              class="field-input"
                            />
                          </div>
                        </template>
                        <template v-else>
                          <InputText
                            :modelValue="getWorkBullet(lang, recordId, bIdx - 1, activeLevels[0])"
                            @update:modelValue="setWorkBullet(lang, recordId, bIdx - 1, activeLevels[0], $event)"
                            class="field-input"
                          />
                        </template>
                      </div>
                    </template>
                  </section>
                </template>
              </div>
            </div>
          </template>
          <div v-if="hasVariants" class="tab-footer">
            <div class="vue-alert vue-alert-info" style="margin-bottom:16px;">
              <i class="pi pi-info-circle" style="margin-top:2px;"></i>
              <span>{{ helpText }}</span>
            </div>
            <div v-if="showLevelChips && warningText" class="vue-alert vue-alert-warning" style="margin-bottom:16px;">
              <i class="pi pi-exclamation-triangle" style="margin-top:2px;"></i>
              <span>{{ warningText }}</span>
            </div>
            <AdaptationLevelRadioGroup
              v-if="showLevelChips"
              v-model="selectedLevel"
              :title="$t('generate.review.chooseLevelToSave')"
              style="margin-bottom:16px;"
            />
            <div class="footer-actions">
              <div class="footer-nav-left">
                <Button
                  :label="$t('generate.review.previousButton.toPositioning')"
                  icon="pi pi-arrow-left"
                  class="p-button-outlined"
                  @click="prevTab"
                />
              </div>
              <div class="footer-nav-center">
                <Button
                  :label="$t('generate.review.saveToPdf')"
                  icon="pi pi-save"
                  class="p-button-success p-button-lg"
                  @click="$emit('save')"
                />
              </div>
              <div class="footer-nav-right">
                <Button
                  :label="$t('generate.review.nextButton.toCourses')"
                  icon="pi pi-arrow-right"
                  iconPos="right"
                  class="p-button-outlined"
                  @click="nextTab"
                />
              </div>
            </div>
          </div>
        </div>
      </TabPanel>

      <!-- Tab 2: Courses and Certifications -->
      <TabPanel :header="$t('generate.review.tabs.courses')" value="2">
        <div class="tab-content">
          <div v-if="!hasVariants" class="vue-card" style="text-align:center;padding:48px;">
            <p>{{ $t('generate.review.noContent') }}</p>
          </div>
          <template v-if="hasVariants">
            <h3 class="vue-h3 section-heading">{{ $t('generate.review.tabs.courses') }}</h3>
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
                        <label class="vue-form-label">{{ $t('generate.review.fields.' + field.key) }}</label>
                        <template v-if="showLevelChips">
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
          </template>
          <div v-if="hasVariants" class="tab-footer">
            <div class="vue-alert vue-alert-info" style="margin-bottom:16px;">
              <i class="pi pi-info-circle" style="margin-top:2px;"></i>
              <span>{{ helpText }}</span>
            </div>
            <div v-if="showLevelChips && warningText" class="vue-alert vue-alert-warning" style="margin-bottom:16px;">
              <i class="pi pi-exclamation-triangle" style="margin-top:2px;"></i>
              <span>{{ warningText }}</span>
            </div>
            <AdaptationLevelRadioGroup
              v-if="showLevelChips"
              v-model="selectedLevel"
              :title="$t('generate.review.chooseLevelToSave')"
              style="margin-bottom:16px;"
            />
            <div class="footer-actions">
              <div class="footer-nav-left">
                <Button
                  :label="$t('generate.review.previousButton.toWork')"
                  icon="pi pi-arrow-left"
                  class="p-button-outlined"
                  @click="prevTab"
                />
              </div>
              <div class="footer-nav-center">
                <Button
                  :label="$t('generate.review.saveToPdf')"
                  icon="pi pi-save"
                  class="p-button-success p-button-lg"
                  @click="$emit('save')"
                />
              </div>
              <div class="footer-nav-right">
                <Button
                  :label="$t('generate.review.nextButton.toProjects')"
                  icon="pi pi-arrow-right"
                  iconPos="right"
                  class="p-button-outlined"
                  @click="nextTab"
                />
              </div>
            </div>
          </div>
        </div>
      </TabPanel>

      <!-- Tab 3: Projects and Volunteering -->
      <TabPanel :header="$t('generate.review.tabs.projects')" value="3">
        <div class="tab-content">
          <div v-if="!hasVariants" class="vue-card" style="text-align:center;padding:48px;">
            <p>{{ $t('generate.review.noContent') }}</p>
          </div>
          <template v-if="hasVariants">
            <h3 class="vue-h3 section-heading">{{ $t('generate.review.tabs.projects') }}</h3>
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
                      <div v-if="field.key !== '__bullets__'" class="vue-card field-card">
                        <label class="vue-form-label">{{ $t('generate.review.fields.' + field.key) }}</label>
                        <template v-if="showLevelChips">
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
                    <!-- Bullets -->
                    <template v-for="bIdx in projectBulletCount(recordId)" :key="'b' + bIdx">
                      <div class="vue-card field-card">
                        <label class="vue-form-label">{{ $t('generate.review.fields.bullet') }} {{ bIdx }}</label>
                        <template v-if="showLevelChips">
                          <div v-for="level in activeLevels" :key="level" class="review-level-variant">
                            <span class="vue-chip chip-level">{{ levelLabel(level) }}</span>
                            <InputText
                              :modelValue="getProjectBullet(lang, recordId, bIdx - 1, level)"
                              @update:modelValue="setProjectBullet(lang, recordId, bIdx - 1, level, $event)"
                              class="field-input"
                            />
                          </div>
                        </template>
                        <template v-else>
                          <InputText
                            :modelValue="getProjectBullet(lang, recordId, bIdx - 1, activeLevels[0])"
                            @update:modelValue="setProjectBullet(lang, recordId, bIdx - 1, activeLevels[0], $event)"
                            class="field-input"
                          />
                        </template>
                      </div>
                    </template>
                  </section>
                </template>
              </div>
            </div>
          </template>
          <div v-if="hasVariants" class="tab-footer">
            <div class="vue-alert vue-alert-info" style="margin-bottom:16px;">
              <i class="pi pi-info-circle" style="margin-top:2px;"></i>
              <span>{{ helpText }}</span>
            </div>
            <div v-if="showLevelChips && warningText" class="vue-alert vue-alert-warning" style="margin-bottom:16px;">
              <i class="pi pi-exclamation-triangle" style="margin-top:2px;"></i>
              <span>{{ warningText }}</span>
            </div>
            <AdaptationLevelRadioGroup
              v-if="showLevelChips"
              v-model="selectedLevel"
              :title="$t('generate.review.chooseLevelToSave')"
              style="margin-bottom:16px;"
            />
            <div class="footer-actions">
              <div class="footer-nav-left">
                <Button
                  :label="$t('generate.review.previousButton.toCourses')"
                  icon="pi pi-arrow-left"
                  class="p-button-outlined"
                  @click="prevTab"
                />
              </div>
              <div class="footer-nav-center">
                <Button
                  :label="$t('generate.review.saveToPdf')"
                  icon="pi pi-save"
                  class="p-button-success p-button-lg"
                  @click="$emit('save')"
                />
              </div>
              <div class="footer-nav-right">
                <Button
                  :label="$t('generate.review.nextButton.toSkills')"
                  icon="pi pi-arrow-right"
                  iconPos="right"
                  class="p-button-outlined"
                  @click="nextTab"
                />
              </div>
            </div>
          </div>
        </div>
      </TabPanel>

      <!-- Tab 4: Skills -->
      <TabPanel :header="$t('generate.review.tabs.skills')" value="4">
        <div class="tab-content">
          <div v-if="!hasVariants" class="vue-card" style="text-align:center;padding:48px;">
            <p>{{ $t('generate.review.noContent') }}</p>
          </div>
          <template v-if="hasVariants">
            <h3 class="vue-h3 section-heading">{{ $t('generate.review.tabs.skills') }}</h3>
            <div class="review-language-grid" :class="{ 'single-language': visibleLanguages.length <= 1 }">
              <template v-for="lang in visibleLanguages" :key="lang">
                <section class="review-language-card">
                  <div v-if="visibleLanguages.length > 1" class="lang-card-header">
                    <span class="vue-chip" :class="lang === 'EN' ? 'chip-en' : 'chip-ru'">{{ lang }}</span>
                    <span class="lang-card-title">{{ languageCardTitle(lang) }}</span>
                  </div>
                  <div v-for="(group, gIdx) in getSkillGroups(lang, activeLevels[0])" :key="gIdx" class="vue-card field-card field-card-skill">
                    <!-- Skill group name -->
                    <div class="vue-form-group" style="margin-bottom:8px;">
                      <label class="vue-form-label">{{ $t('generate.review.fields.skillGroup') }}</label>
                      <template v-if="showLevelChips">
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
                    <!-- Skills as comma-separated input -->
                    <div class="vue-form-group">
                      <label class="vue-form-label vue-label-sm">{{ $t('generate.review.fields.skill') }}</label>
                      <template v-if="showLevelChips">
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
          </template>
          <div v-if="hasVariants" class="tab-footer">
            <div class="vue-alert vue-alert-info" style="margin-bottom:16px;">
              <i class="pi pi-info-circle" style="margin-top:2px;"></i>
              <span>{{ helpText }}</span>
            </div>
            <div v-if="showLevelChips && warningText" class="vue-alert vue-alert-warning" style="margin-bottom:16px;">
              <i class="pi pi-exclamation-triangle" style="margin-top:2px;"></i>
              <span>{{ warningText }}</span>
            </div>
            <AdaptationLevelRadioGroup
              v-if="showLevelChips"
              v-model="selectedLevel"
              :title="$t('generate.review.chooseLevelToSave')"
              style="margin-bottom:16px;"
            />
            <div class="footer-actions">
              <div class="footer-nav-left">
                <Button
                  :label="$t('generate.review.previousButton.toProjects')"
                  icon="pi pi-arrow-left"
                  class="p-button-outlined"
                  @click="prevTab"
                />
              </div>
              <div class="footer-nav-center">
                <Button
                  :label="$t('generate.review.saveToPdf')"
                  icon="pi pi-save"
                  class="p-button-success p-button-lg"
                  @click="$emit('save')"
                />
              </div>
              <div class="footer-nav-right">
                <Button
                  :label="$t('generate.review.nextButton.toPersonal')"
                  icon="pi pi-arrow-right"
                  iconPos="right"
                  class="p-button-outlined"
                  @click="nextTab"
                />
              </div>
            </div>
          </div>
        </div>
      </TabPanel>

      <!-- Tab 5: Personal Information -->
      <TabPanel :header="$t('generate.review.tabs.personal')" value="5">
        <div class="tab-content">
          <div v-if="!hasVariants" class="vue-card" style="text-align:center;padding:48px;">
            <p>{{ $t('generate.review.noContent') }}</p>
          </div>
          <template v-if="hasVariants">
            <h3 class="vue-h3 section-heading">{{ $t('generate.review.tabs.personal') }}</h3>
            <div class="review-language-grid" :class="{ 'single-language': visibleLanguages.length <= 1 }">
              <template v-for="lang in visibleLanguages" :key="lang">
                <section class="review-language-card">
                  <div v-if="visibleLanguages.length > 1" class="lang-card-header">
                    <span class="vue-chip" :class="lang === 'EN' ? 'chip-en' : 'chip-ru'">{{ lang }}</span>
                    <span class="lang-card-title">{{ languageCardTitle(lang) }}</span>
                  </div>
                  <template v-for="field in personalFields" :key="field.key">
                    <div class="vue-card field-card">
                      <label class="vue-form-label">{{ $t('generate.review.fields.' + field.key) }}</label>
                      <template v-if="showLevelChips">
                        <div v-for="level in activeLevels" :key="level" class="review-level-variant">
                          <span class="vue-chip chip-level">{{ levelLabel(level) }}</span>
                          <InputText
                            :modelValue="getPersonalField(lang, field.key, level)"
                            @update:modelValue="setPersonalField(lang, field.key, level, $event)"
                            class="field-input"
                          />
                        </div>
                      </template>
                      <template v-else>
                        <InputText
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
          </template>
          <div v-if="hasVariants" class="tab-footer">
            <div class="vue-alert vue-alert-info" style="margin-bottom:16px;">
              <i class="pi pi-info-circle" style="margin-top:2px;"></i>
              <span>{{ helpText }}</span>
            </div>
            <div v-if="showLevelChips && warningText" class="vue-alert vue-alert-warning" style="margin-bottom:16px;">
              <i class="pi pi-exclamation-triangle" style="margin-top:2px;"></i>
              <span>{{ warningText }}</span>
            </div>
            <AdaptationLevelRadioGroup
              v-if="showLevelChips"
              v-model="selectedLevel"
              :title="$t('generate.review.chooseLevelToSave')"
              style="margin-bottom:16px;"
            />
            <div class="footer-actions">
              <div class="footer-nav-left">
                <Button
                  :label="$t('generate.review.previousButton.toSkills')"
                  icon="pi pi-arrow-left"
                  class="p-button-outlined"
                  @click="prevTab"
                />
              </div>
              <div class="footer-nav-center">
                <Button
                  :label="$t('generate.review.saveToPdf')"
                  icon="pi pi-save"
                  class="p-button-success p-button-lg"
                  @click="$emit('save')"
                />
              </div>
              <div class="footer-nav-right"></div>
            </div>
          </div>
        </div>
      </TabPanel>
    </TabView>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { GeneratedVariant, AdaptationLevel, GeneratedSkillGroup } from '@/types/generate'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import InputText from 'primevue/inputtext'
import Textarea from 'primevue/textarea'
import Button from 'primevue/button'
import AdaptationLevelRadioGroup from './AdaptationLevelRadioGroup.vue'

const { t } = useI18n()

const props = defineProps<{
  enVariants: GeneratedVariant[]
  ruVariants: GeneratedVariant[]
  isBilingual: boolean
  showLevels: boolean
  activeTab: number
  selectedLevel: AdaptationLevel
}>()

const emit = defineEmits<{
  (e: 'update:activeTab', val: number): void
  (e: 'update:selectedLevel', val: AdaptationLevel): void
  (e: 'save'): void
}>()

const selectedLevel = computed({
  get: () => props.selectedLevel,
  set: (val) => emit('update:selectedLevel', val)
})

const activeTab = computed({
  get: () => props.activeTab,
  set: (val) => emit('update:activeTab', val)
})

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

const showLevelChips = computed(() => props.showLevels)

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

// ─── Field definitions ────────────────────────────────────────────────

const input = { type: 'input' as const }
const textarea = (rows: number) => ({ type: 'textarea' as const, rows })

const positioningFields = [
  { key: 'professionalTitle' as const, ...input },
  { key: 'valueLine' as const, ...textarea(2) },
  { key: 'professionalSummary' as const, ...textarea(4) },
  { key: 'professionalAspirations' as const, ...textarea(3) },
  { key: 'coverLetter' as const, ...textarea(6) },
]

const workFields = [
  { key: 'jobTitle' as const, ...input },
  { key: 'companyName' as const, ...input },
  { key: 'description' as const, ...textarea(3) },
  { key: '__bullets__' as const, ...input },
]

const courseFields = [
  { key: 'courseName' as const, ...input },
  { key: 'provider' as const, ...input },
  { key: 'courseFocus' as const, ...textarea(2) },
]

const projectFields = [
  { key: 'projectName' as const, ...input },
  { key: 'role' as const, ...input },
  { key: 'description' as const, ...textarea(3) },
  { key: '__bullets__' as const, ...input },
]

const personalFields = [
  { key: 'location' as const, ...input },
  { key: 'spokenLanguages' as const, ...input },
  { key: 'willingnessToRelocate' as const, ...input },
  { key: 'willingnessForBusinessTrips' as const, ...input },
  { key: 'citizenship' as const, ...input },
  { key: 'dateOfBirth' as const, ...input },
  { key: 'workFormats' as const, ...input },
]

// ─── Localization helpers ─────────────────────────────────────────────

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

// ─── Variant helper ───────────────────────────────────────────────────

function findVariant(lang: string, level: string): GeneratedVariant | undefined {
  const source = lang === 'EN' ? props.enVariants : props.ruVariants
  return source.find(v => v.adaptationLevel === level)
}

// ─── Simple field get/set (positioning) ───────────────────────────────

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

// ─── Work Experience ──────────────────────────────────────────────────

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

function workBulletCount(sourceId: string): number {
  let max = 0
  for (const lvl of activeLevels.value) {
    for (const lang of visibleLanguages.value) {
      const variant = findVariant(lang, lvl)
      if (variant) {
        const record = variant.workExperience.find(e => e.sourceId === sourceId)
        if (record && record.bullets.length > max) {
          max = record.bullets.length
        }
      }
    }
  }
  return max
}

function getWorkBullet(lang: string, sourceId: string, idx: number, level: string): string {
  const variant = findVariant(lang, level)
  if (!variant) return ''
  const record = variant.workExperience.find(e => e.sourceId === sourceId)
  if (!record || idx >= record.bullets.length) return ''
  return record.bullets[idx] ?? ''
}

function setWorkBullet(lang: string, sourceId: string, idx: number, level: string, val: any) {
  const variant = findVariant(lang, level)
  if (variant && val !== undefined) {
    const record = variant.workExperience.find(e => e.sourceId === sourceId)
    if (record && idx < record.bullets.length) {
      record.bullets[idx] = val
    }
  }
}

// ─── Courses ──────────────────────────────────────────────────────────

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

// ─── Projects ─────────────────────────────────────────────────────────

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

function projectBulletCount(sourceId: string): number {
  let max = 0
  for (const lvl of activeLevels.value) {
    for (const lang of visibleLanguages.value) {
      const variant = findVariant(lang, lvl)
      if (variant) {
        const record = variant.projects.find(p => p.sourceId === sourceId)
        if (record && record.bullets.length > max) {
          max = record.bullets.length
        }
      }
    }
  }
  return max
}

function getProjectBullet(lang: string, sourceId: string, idx: number, level: string): string {
  const variant = findVariant(lang, level)
  if (!variant) return ''
  const record = variant.projects.find(p => p.sourceId === sourceId)
  if (!record || idx >= record.bullets.length) return ''
  return record.bullets[idx] ?? ''
}

function setProjectBullet(lang: string, sourceId: string, idx: number, level: string, val: any) {
  const variant = findVariant(lang, level)
  if (variant && val !== undefined) {
    const record = variant.projects.find(p => p.sourceId === sourceId)
    if (record && idx < record.bullets.length) {
      record.bullets[idx] = val
    }
  }
}

// ─── Personal Information ───────────────────────────────────────────────

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

// ─── Skills ───────────────────────────────────────────────────────────

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

// ─── Navigation ───────────────────────────────────────────────────────

function prevTab() {
  if (activeTab.value > 0) {
    activeTab.value--
  }
}

function nextTab() {
  if (activeTab.value < 5) {
    activeTab.value++
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
.review-level-variant .field-input,
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

/* ─── Mobile: vertical tab navigation ─────────────────────────── */
@media (max-width: 768px) {
  .review-tabs :deep(.p-tabview-tablist-container) {
    overflow: visible !important;
  }
  .review-tabs :deep(.p-tabview-tablist-scroll-container) {
    overflow: visible !important;
    width: 100% !important;
  }
  .review-tabs :deep(.p-tabview-tablist) {
    display: flex !important;
    flex-direction: column !important;
    align-items: stretch !important;
    width: 100% !important;
    gap: 8px !important;
    border-bottom: 0 !important;
  }
  .review-tabs :deep(.p-tabview-tablist-item) {
    width: 100% !important;
  }
  .review-tabs :deep(.p-tabview-tab-header) {
    width: 100% !important;
    justify-content: flex-start !important;
    border: 1px solid var(--vue-border-soft) !important;
    border-radius: var(--vue-radius-md) !important;
    margin: 0 !important;
    background: var(--vue-bg-surface) !important;
  }
  .review-tabs :deep(.p-tabview-tablist-item-active .p-tabview-tab-header) {
    border-color: var(--vue-accent-primary) !important;
    background: var(--vue-accent-bg-primary) !important;
    color: var(--vue-accent-primary) !important;
  }
  .review-tabs :deep(.p-tabview-ink-bar) {
    display: none !important;
    width: 0 !important;
  }
}
</style>
