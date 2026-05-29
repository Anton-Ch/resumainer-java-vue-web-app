# Wireframe Field Requirements

**Project ID:** `resumainer`  
**Product Name:** ResumAIner  
**Date Created:** 2026-05-13  
**Last Updated:** 2026-05-16  
**Author:** Anton  
**Version:** 2.0  
**Status:** Active  
**Related BABOK Area:** 7.1 Specify and Model Requirements  

---

## 1. Description

This document captures field-level requirements identified during early stage of wireframe preparation.

It connects UI design, validation rules, error messages, and draft data model notes. The document is not a final database schema. It is a practical input for UI/UX requirements, ERD, implementation planning, and test case design.

## 2. Usage Rules

- Use this document as the field-level source for wireframes and form requirements.
- Keep final database decisions in the data model / ERD artifacts.
- Keep field names stable once they are used in requirements and traceability.
- Validation rules here should be reflected in acceptance criteria and tests.
- If a field changes significantly, update the Change Request Log.

## 3. Global UI Pattern Decisions

### 3.1 Repeatable Profile Sections

The following sections use card list + Add/Edit form pattern:
- Work Experience;
- Projects & Volunteering;
- Education;
- Courses & Certificates.

Common pattern:
- section title;
- short hint;
- `+ Add` button;
- record cards;
- card preview with key fields;
- Edit action;
- Delete action;
- Add/Edit form.

### 3.2 Sorting Rules

Repeatable records are sorted automatically in MVP.

| Section                 | Sorting Rule                                                                   |
| ----------------------- | ------------------------------------------------------------------------------ |
| Work Experience         | Current role first, then end date descending, then start date descending       |
| Projects & Volunteering | Ongoing projects first, then end date descending, then start date descending   |
| Education               | Currently studying first, then end year descending, then start year descending |
| Courses & Certificates  | Start date descending, then end date descending                                |

## 4. My Profile Field Requirements

### 4.1 Contact Details

#### Fields

| Field | Required | Type | Notes |
|---|---|---|---|
| Full name | Yes | Text | Candidate name for resume |
| Professional title | No | Text | Example: Business Analyst, Junior Java Developer |
| Email | Yes | Email | Main contact email |
| Phone | Yes | Text | Phone number as text to preserve formatting |
| Location | Yes | Text | Country + city, e.g. Kazakhstan, Astana |
| LinkedIn URL | No | URL | Professional profile link |
| Portfolio / Website URL | No | URL | Personal website, portfolio, GitHub Pages, etc. |
| Telegram | No | Text | Username or contact value |
| WhatsApp | No | Text | WhatsApp contact value |

#### Validation Rules

| Field | Validation |
|---|---|
| Full name | Required, 2–100 characters |
| Professional title | Optional, max 120 characters |
| Email | Required, valid email format |
| Phone | Required, 5–30 characters |
| Location | Required, max 100 characters |
| LinkedIn URL | Optional, valid URL if provided |
| Portfolio / Website URL | Optional, valid URL if provided |
| Telegram / WhatsApp | Optional, max 100 characters |

#### Error Messages

~~~text
Full name is required.
Email is required.
Please enter a valid email address.
Phone is required.
Phone number is too long.
Location is required.
Please enter a valid LinkedIn URL.
Please enter a valid portfolio URL.
Messenger value is too long.
~~~

#### Draft Data Model Notes

~~~text
contact_details
---------------
id
user_id
full_name
professional_title
email
phone
location
linkedin_url
portfolio_url
telegram
whatsapp
created_at
updated_at
is_deleted
deleted_at
~~~

### 4.2 Work Experience

#### Fields

| Field | Required | Type | Notes |
|---|---|---|---|
| Job title | Yes | Text | Position name |
| Company name | Yes | Text | Employer name |
| Location | No | Text | City/country or Remote |
| Start date | Yes | Date | Role start date |
| End date | Conditional | Date | Not required for current role |
| Role and job description | Yes | Textarea | Responsibilities, achievements, tools, measurable results |
| Company URL | No | URL | Employer website |

#### Validation Rules

| Field | Validation |
|---|---|
| Job title | Required, 2–120 characters |
| Company name | Required, 2–120 characters |
| Location | Optional, max 120 characters |
| Start date | Required |
| End date | Optional if current role |
| Date range | End date must not be before start date |
| Role and job description | Required, max 5000 characters |
| Company URL | Optional, valid URL if provided |

#### Error Messages

~~~text
Job title is required.
Company name is required.
Start date is required.
End date cannot be earlier than start date.
Role and job description is required.
Role and job description is too long.
Please enter a valid company URL.
~~~

#### Draft Data Model Notes

~~~text
work_experience
---------------
id
user_id
job_title
company_name
location
start_date
end_date
is_current
summary
company_url
created_at
updated_at
is_deleted
deleted_at
~~~

### 4.3 Projects & Volunteering

#### Fields

| Field | Required | Type | Notes |
|---|---|---|---|
| Project name | Yes | Text | Project or volunteering initiative name |
| Role | No | Text | User role in the project |
| Start date | No | Date | Project start date |
| End date | No | Date | Project end date if finished |
| Description | Yes | Textarea | Project goal, contribution, tools, results |
| Project URL | No | URL | Repository, website, portfolio link |

#### Validation Rules

| Field | Validation |
|---|---|
| Project name | Required, 2–150 characters |
| Role | Optional, max 120 characters |
| Start date | Optional |
| End date | Optional |
| Date range | End date must not be before start date |
| Description | Required, max 5000 characters |
| Project URL | Optional, valid URL if provided |

#### Error Messages

~~~text
Project name is required.
Description is required.
End date cannot be earlier than start date.
Project description is too long.
Please enter a valid project or repository URL.
~~~

#### Draft Data Model Notes

~~~text
projects
--------
id
user_id
project_name
role
start_date
end_date
description
project_url
created_at
updated_at
is_deleted
deleted_at
~~~

### 4.4 Education

#### Fields

| Field | Required | Type | Notes |
|---|---|---|---|
| Institution name | Yes | Text | University, college, school |
| Degree / Qualification | Yes | Text | Bachelor, Master, Diploma, Certificate, etc. |
| Field of study / Major | No | Text | Example: Information Systems |
| Start year | Yes | Year | Education start year |
| End year | Conditional | Year | Not required if currently studying |
| Location | No | Text | City/country |
| Comment / Description | No | Textarea | Relevant courses, thesis, focus area |
| GPA / Grade | No | Text | Optional academic result |

#### Validation Rules

| Field | Validation |
|---|---|
| Institution name | Required, 2–150 characters |
| Degree / Qualification | Required, 2–150 characters |
| Field of study / Major | Optional, max 150 characters |
| Start year | Required, valid year |
| End year | Optional, valid year |
| Date range | End year must not be earlier than start year |
| Location | Optional, max 120 characters |
| Description | Optional, max 3500 characters |
| GPA / Grade | Optional, max 50 characters |

#### Error Messages

~~~text
Institution name is required.
Degree / qualification is required.
Start year is required.
Start year must be a valid year.
End year must be a valid year.
End year cannot be earlier than start year.
Description is too long.
~~~

#### Draft Data Model Notes

~~~text
education
---------
id
user_id
institution_name
degree
field_of_study
location
start_year
end_year
is_current
description
gpa
created_at
updated_at
is_deleted
deleted_at
~~~

### 4.5 Courses & Certificates

#### Fields

| Field | Required | Type | Notes |
|---|---|---|---|
| Course / Certificate name | Yes | Text | Course or certificate title |
| Provider / Issuer | Yes | Text | Coursera, Microsoft, LinkedIn Learning, etc. |
| Start date | Yes | Date | Useful for in-progress learning |
| End date | No | Date | Completion date if available |
| Credential URL | No | URL | Certificate link |
| Skills / Topics | No | Text | Java, SQL, Business Analysis, Spring MVC, etc. |
| Short description | No | Textarea | What was learned and why it is relevant |

#### Validation Rules

| Field | Validation |
|---|---|
| Course / Certificate name | Required, 2–250 characters |
| Provider / Issuer | Required, 2–150 characters |
| Start date | Required, valid date |
| End date | Optional, valid date |
| Date range | End date must not be earlier than start date |
| Credential URL | Optional, valid URL if provided |
| Skills / Topics | Optional, max 2000 characters |
| Description | Optional, max 5000 characters |

#### Error Messages

~~~text
Course or certificate name is required.
Provider or issuer is required.
Start date is required.
Completion date cannot be earlier than start date.
Please enter a valid credential URL.
Skills / topics text is too long.
Description is too long.
~~~

#### Draft Data Model Notes

~~~text
courses_certificates
--------------------
id
user_id
title
provider
start_date
end_date
credential_url
skills
description
created_at
updated_at
is_deleted
deleted_at
~~~

### 4.6 Additional Info and Settings

#### Fields

| Field | Required | Type | Notes |
|---|---|---|---|
| Skills | No | Textarea | General skills for AI context |
| Languages | No | Textarea | Languages spoken by user |
| Professional aspirations | No | Textarea | Desired career direction |
| Achievements | No | Textarea | Achievements not tied to specific job/project |
| Default resume language | No | Dropdown | Default language for generation |
| Additional resume language | No | Dropdown | Optional second language |
| General information | No | Textarea | Additional AI context |
| Profile picture | No | File | Optional image |
| Date of birth | No | Date | Candidate date of birth |
| Ready for relocation | No | Dropdown | Yes / No / Not specified |
| Ready for business trips and rotational schedule | No | Dropdown | Yes / No / Not specified |
| Preferred work format | No | Checkbox group | Full-time, Part-time, Offline, Remote, Hybrid, On-site project based |
| Username | Yes | Text | Unique URL-friendly value for public resume links |

#### Validation Rules

| Field | Validation |
|---|---|
| Skills | Optional, max 3000 characters |
| Languages | Optional, max 1000 characters |
| Professional aspirations | Optional, max 3000 characters |
| Achievements | Optional, max 3000 characters |
| Default resume language | Optional, valid dropdown value |
| Additional resume language | Optional, valid dropdown value and different from default if provided |
| General information | Optional, max 10000 characters |
| Profile picture | Optional, valid image type and size if provided |
| Date of birth | Optional, valid date |
| Ready for relocation | Optional, valid dropdown value (Yes / No / Not specified) |
| Ready for business trips | Optional, valid dropdown value (Yes / No / Not specified) |
| Preferred work format | Optional, valid checkbox values (full-time, part-time, offline, remote, hybrid, on-site project based) |
| Username | Required, URL-friendly, unique |

#### Error Messages

~~~text
Skills text is too long.
Languages text is too long.
Professional aspirations text is too long.
Achievements text is too long.
Default resume language has invalid value.
Additional resume language has invalid value.
Additional resume language must be different from default resume language.
General information text is too long.
Please upload a valid image file.
Username is required.
Username must be URL-friendly.
Username is already taken.
Date of birth must be a valid date.
Preferred work format has invalid value.
~~~

#### Draft Data Model Notes

~~~text
additional_profile_info
-----------------------
id
user_id
skills
languages
professional_aspirations
achievements
default_resume_language
additional_resume_language
general_info
date_of_birth
ready_for_relocation
ready_for_business_trips
preferred_work_format
profile_picture_path
username
created_at
updated_at
is_deleted
deleted_at
~~~

### 5. Generate Resume Field Requirements

#### Fields

| Field | Required | Type | Notes |
|---|---|---|---|
| Vacancy description | Yes | Textarea | Target vacancy text |
| Company description | No | Textarea | Target employer context |
| Professional aspirations | No | Textarea | Optional user goal for this application |
| Language | Yes | Dropdown | Output resume language |
| Adaptation level | Yes | Dropdown | Minimal, Balanced, Maximum |
| AI model | Yes | Dropdown | Active model selected for generation |
| Additional comments for AI | No | Textarea | Extra instructions for resume generation |
| Include cover letter | No | Checkbox / Toggle | Include cover letter generation with resume |

#### Validation Rules

| Field | Validation |
|---|---|
| Vacancy description | Required, max 20000 characters |
| Company description | Optional, max 10000 characters |
| Professional aspirations | Optional, max 3000 characters |
| Language | Required, valid dropdown value |
| Adaptation level | Required, valid dropdown value |
| AI model | Required, active model only |
| Additional comments for AI | Optional, max 10000 characters |
| Include cover letter | Optional, boolean |

#### Error Messages

~~~text
Vacancy description is required.
Vacancy description is too long.
Company description is too long.
Professional aspirations text is too long.
Please select resume language.
Please select adaptation level.
Please select active AI model.
Additional comments text is too long.
Selected AI model is inactive or unavailable.
~~~

#### Draft Data Model Notes

~~~text
resume_generation_requests
--------------------------
id
user_id
ai_model_id
vacancy_description
company_information
professional_aspirations
resume_language
adaptation_level
additional_comments
include_cover_letter
status
error_message
created_at
updated_at
is_deleted
deleted_at
~~~

#### Status Values

~~~text
Draft
Pending
Processing
Completed
Failed
Cancelled
~~~

#### Possible API Routes

~~~text
GET  /api/profile/readiness
GET  /api/ai-models/active
POST /api/resume-generation-requests
GET  /api/resume-generation-requests/{id}
~~~

### 6. Resume Review Field Requirements

#### Fields

| Field | Required | Type | Notes |
|---|---|---|---|
| Professional summary | Generated | Text | AI-generated, editable |
| Professional role | Generated | Text | AI-generated, editable |
| Professional aspiration | Generated | Text | AI-generated, editable |
| Work Experience rows | Generated | Repeatable | AI-generated, editable |
| Skills rows | Generated | Repeatable | AI-generated, editable |
| Courses & Certificates rows | Generated | Repeatable | AI-generated, editable |
| Projects & Volunteering rows | Generated | Repeatable | AI-generated, editable |
| Cover letter text | Generated | Textarea | AI-generated, editable before saving |

#### Validation Rules

| Field | Validation |
|---|---|
| Professional summary | Editable, max 5000 characters |
| Professional role | Editable, max 200 characters |
| Professional aspiration | Editable, max 1000 characters |
| Work Experience rows | Editable sections |
| Skills rows | Editable sections |
| Courses & Certificates rows | Editable sections |
| Projects & Volunteering rows | Editable sections |
| Cover letter text | Editable, max 10000 characters |

#### Error Messages

~~~text
Cover letter text is too long.
~~~

#### Main Action

| Button | Description |
|---|---|
| Save & Create | Saves final resume version, creates PDF and public link |

#### Draft Data Model Notes

~~~text
saved_resumes
-------------
id
user_id
source_draft_id
adaptation_level_id
language_id
title
final_content
cover_letter
public_code
is_public
is_deleted
created_at
updated_at
deleted_at
~~~

---

## 7. Summary

These field requirements should be used as a practical bridge between wireframes, validation rules, data model refinement, and implementation planning.

They are approved as MVP field-level input but may still be refined during ERD and implementation design.
