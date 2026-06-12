from __future__ import annotations

import html
import re
import sqlite3
from datetime import datetime
from pathlib import Path
from typing import Any

from app.dao.generation_response_dao import get_response_bundle
from app.dao.profile_dao import get_profile_payload
from app.utils.file_utils import safe_write

# PROTOTYPE PATCH v3.1
# This renderer intentionally uses the restored one_page/two_page HTML templates.
# It does NOT build a simplified debug HTML document from scratch anymore.
# Java/OpenCode target: ResumeTemplateRenderer must fill the same saved HTML template
# before PdfGenerationService / HtmlToPdfConverter converts it to PDF.

ALLOWED_HTML_TAGS = {"p", "strong", "em", "ul", "li", "br"}

SECTION_TITLES = {
    "EN": {
        "summary": "Professional Summary",
        "work": "Work Experience",
        "skills": "Skills",
        "education": "Education",
        "courses": "Courses and Certifications",
        "projects": "Projects and Volunteering",
        "aspirations": "Professional Aspirations",
        "personal": "Personal Information",
        "next": "See the next page",
        "previous": "See the previous page",
        "continued": "Continued Resume",
    },
    "RU": {
        "summary": "О себе",
        "work": "Опыт работы",
        "skills": "Навыки",
        "education": "Образование",
        "courses": "Курсы и сертификаты",
        "projects": "Проекты и волонтёрство",
        "aspirations": "Профессиональные цели",
        "personal": "Личная информация",
        "next": "См. следующую страницу",
        "previous": "См. предыдущую страницу",
        "continued": "Продолжение резюме",
    },
}

PERSONAL_LABELS = {
    "EN": {
        "location": "Location",
        "languages": "Languages",
        "relocation": "Relocation",
        "businessTrips": "Business trips",
        "workFormat": "Work format",
        "citizenship": "Citizenship",
        "dateOfBirth": "Date of birth",
        "gpaGrade": "GPA / Grade",
    },
    "RU": {
        "location": "Локация",
        "languages": "Языки",
        "relocation": "Переезд",
        "businessTrips": "Командировки",
        "workFormat": "Формат работы",
        "citizenship": "Гражданство",
        "dateOfBirth": "Дата рождения",
        "gpaGrade": "GPA / оценка",
    },
}


def render_response_to_html(
    conn: sqlite3.Connection,
    response_id: int,
    output_dir: str | Path = "output/html",
) -> str:
    """Render a generated response into the restored A4 HTML template and save it to disk."""
    bundle = get_response_bundle(conn, response_id)
    response = bundle["response"]
    request = conn.execute(
        "SELECT * FROM resume_generation_request WHERE id = ?",
        (response["generation_request_id"],),
    ).fetchone()
    if not request:
        raise RuntimeError(f"Generation request not found for response_id={response_id}")

    profile = get_profile_payload(conn, str(request["user_id"]))
    language_code = str(response["language_code"]).upper()
    adaptation_level = str(response["adaptation_level_code"]).lower()

    project_root = Path(__file__).resolve().parents[2]
    template_mode = choose_template_mode(bundle, profile)
    template_path = project_root / template_name(template_mode, language_code)
    if not template_path.exists():
        raise RuntimeError(
            f"Template not found: {template_path}. "
            "Put restored one_page_template_*.html and two_page_template_*.html files into backend_v3 project root."
        )

    template_text = template_path.read_text(encoding="utf-8")
    html_text = fill_template(template_text, bundle, profile)

    # Fallback for temporary/debug templates that only contain {{RESUME_CONTENT}}.
    # Restored production-like templates should use <!-- RESUME:* --> markers.
    if "{{RESUME_CONTENT}}" in html_text:
        html_text = html_text.replace("{{RESUME_CONTENT}}", build_fallback_resume_body(bundle, profile))

    timestamp = datetime.now().strftime("%Y-%m-%d-%H-%M")
    filename = f"{timestamp}_{language_code.lower()}_{adaptation_level}.html"
    return safe_write(Path(output_dir) / filename, html_text)


def choose_template_mode(bundle: dict[str, Any], profile: dict[str, Any]) -> str:
    """Small v3.1 rule-based template selection.

    The restored templates do the real visual work. This method only chooses one_page/two_page.
    Conservative default: use two_page when there are projects, >2 work items, or >2 courses.
    """
    work_count = len(bundle.get("experiences", []))
    course_count = len(bundle.get("courses", []))
    project_count = len(bundle.get("projects", []))
    skill_count = len(bundle.get("skill_groups", []))
    education_count = len(profile.get("education", []) or [])
    if project_count > 0 or work_count > 2 or course_count > 2 or skill_count > 3 or education_count > 2:
        return "two_page"
    return "one_page"


def template_name(template_mode: str, language_code: str) -> str:
    lang = language_code.lower()
    if template_mode == "one_page":
        return f"one_page_template_{lang}.html"
    return f"two_page_template_{lang}.html"


def fill_template(template_text: str, bundle: dict[str, Any], profile: dict[str, Any]) -> str:
    fragments = build_fragments(bundle, profile)
    result = template_text
    for marker, replacement in fragments.items():
        result = result.replace(marker, replacement)
    return result


def build_fragments(bundle: dict[str, Any], profile: dict[str, Any]) -> dict[str, str]:
    language_code = str(bundle["response"]["language_code"]).upper()
    return {
        "<!-- RESUME:HEADER -->": render_header(bundle, profile),
        "<!-- RESUME:PROFESSIONAL_SUMMARY -->": render_summary(bundle, language_code),
        "<!-- RESUME:WORK_EXPERIENCE_PRIMARY -->": render_experience(bundle, language_code, primary=True),
        "<!-- RESUME:SKILLS -->": render_skills(bundle, language_code),
        "<!-- RESUME:EDUCATION -->": render_education(profile, language_code),
        "<!-- RESUME:COURSES -->": render_courses(bundle, language_code),
        "<!-- RESUME:PAGE_TWO_HEADER -->": render_page_two_header(bundle, profile, language_code),
        "<!-- RESUME:PROJECTS -->": render_projects(bundle, language_code),
        "<!-- RESUME:PROFESSIONAL_ASPIRATIONS -->": render_aspirations(bundle, language_code),
        "<!-- RESUME:WORK_EXPERIENCE_ADDITIONAL -->": render_experience(bundle, language_code, primary=False),
        "<!-- RESUME:PERSONAL_INFO -->": render_personal_info(bundle, profile, language_code),
        "<!-- RESUME:NOTE_NEXT -->": SECTION_TITLES[language_code]["next"],
        "<!-- RESUME:NOTE_PREVIOUS -->": SECTION_TITLES[language_code]["previous"],
    }


def render_header(bundle: dict[str, Any], profile: dict[str, Any]) -> str:
    response = bundle["response"]
    contact = profile.get("contact") or {}
    line_1 = join_non_empty([
        contact.get("phone"),
        contact.get("resume_email"),
        contact.get("linkedin_url"),
        contact.get("location"),
    ])
    line_2 = join_non_empty([
        contact.get("portfolio_url"),
        f"Telegram: {contact.get('telegram')}" if contact.get("telegram") else None,
        f"WhatsApp: {contact.get('whatsapp')}" if contact.get("whatsapp") else None,
    ])
    parts = [
        "<section>",
        f'  <div class="candidate-name">{esc(contact.get("full_name"))}</div>',
        f'  <div class="candidate-title">{esc(response.get("professional_title") or contact.get("professional_title"))}</div>',
        f'  <div class="contact-line">{esc(line_1)}</div>',
    ]
    if line_2:
        parts.append(f'  <div class="contact-line">{esc(line_2)}</div>')
    if response.get("value_line"):
        parts.append(f'  <div class="value-line">{esc(response.get("value_line"))}</div>')
    parts.append("</section>")
    return "\n".join(parts)


def render_page_two_header(bundle: dict[str, Any], profile: dict[str, Any], language_code: str) -> str:
    response = bundle["response"]
    contact = profile.get("contact") or {}
    return (
        "<section>\n"
        f'  <div class="page-two-name">{esc(contact.get("full_name"))}</div>\n'
        f'  <div class="page-two-title">{esc(response.get("professional_title"))} | {esc(SECTION_TITLES[language_code]["continued"])}</div>\n'
        "</section>"
    )


def render_summary(bundle: dict[str, Any], language_code: str) -> str:
    return section(SECTION_TITLES[language_code]["summary"], as_paragraph(bundle["response"].get("professional_summary")))


def render_aspirations(bundle: dict[str, Any], language_code: str) -> str:
    return section(SECTION_TITLES[language_code]["aspirations"], as_paragraph(bundle["response"].get("professional_aspirations")))


def render_experience(bundle: dict[str, Any], language_code: str, primary: bool) -> str:
    # Primary page: first two jobs. Additional page: the rest.
    items = bundle.get("experiences", [])
    selected = items[:2] if primary else items[2:]
    blocks: list[str] = []
    for item in selected:
        bullets = "".join(f"<li>{esc(b)}</li>" for b in item.get("bullets", []) if b)
        bullets_html = f"<ul>{bullets}</ul>" if bullets else ""
        location = f" – {esc(item.get('location'))}" if item.get("location") else ""
        date = f" | <span class=\"date\">{esc(item.get('date_range'))}</span>" if item.get("date_range") else ""
        blocks.append(
            '<div class="job-block">\n'
            f'  <div class="item-heading">{esc(item.get("job_title"))} | {esc(item.get("company_name"))}{location}{date}</div>\n'
            f'  {as_paragraph(item.get("description"))}\n'
            f'  {bullets_html}\n'
            '</div>'
        )
    if not blocks:
        return ""
    return section(SECTION_TITLES[language_code]["work"], "\n".join(blocks))


def render_skills(bundle: dict[str, Any], language_code: str) -> str:
    lines = []
    for group in bundle.get("skill_groups", []):
        skills = ", ".join(str(s) for s in group.get("skills", []) if s)
        lines.append(
            f'<div class="skill-group"><span class="skill-label">{esc(group.get("group_name"))}:</span> {esc(skills)}</div>'
        )
    return section(SECTION_TITLES[language_code]["skills"], "\n".join(lines))


def render_education(profile: dict[str, Any], language_code: str) -> str:
    lines = []
    suffix = "en" if language_code == "EN" else "ru"
    for item in profile.get("education", []) or []:
        years = year_range(item.get("start_date"), item.get("end_date"), language_code)
        institution = item.get(f"institution_name_{suffix}") or item.get("institution_name") or ""
        degree = item.get(f"degree_{suffix}") or item.get("degree") or ""
        field = item.get(f"field_of_study_{suffix}") or item.get("field_of_study") or ""
        if language_code == "EN":
            degree_field = f"{degree} in {field}".strip()
        else:
            degree_field = f"{degree}: {field}".strip(": ")
        # Intentionally no education location: compact layout avoids long-line overflow.
        lines.append(
            '<div class="education-line">'
            f'<strong>{esc(degree_field)}</strong> | {esc(institution)} | {esc(years)}'
            '</div>'
        )
    return section(SECTION_TITLES[language_code]["education"], "\n".join(lines))


def render_courses(bundle: dict[str, Any], language_code: str) -> str:
    lines = []
    for item in bundle.get("courses", []):
        focus = f" | {esc(item.get('course_focus'))}" if item.get("course_focus") else ""
        lines.append(
            '<div class="course-line">'
            f'<strong>{esc(item.get("name"))}</strong> – {esc(item.get("provider"))}{focus}'
            '</div>'
        )
    return section(SECTION_TITLES[language_code]["courses"], "\n".join(lines))


def render_projects(bundle: dict[str, Any], language_code: str) -> str:
    blocks = []
    for item in bundle.get("projects", []):
        bullets = "".join(f"<li>{esc(b)}</li>" for b in item.get("bullets", []) if b)
        bullets_html = f"<ul>{bullets}</ul>" if bullets else ""
        role = f" | {esc(item.get('role'))}" if item.get("role") else ""
        date = f" | <span class=\"date\">{esc(item.get('date_range'))}</span>" if item.get("date_range") else ""
        blocks.append(
            '<div class="project-block">\n'
            f'  <div class="item-heading">{esc(item.get("project_name"))}{role}{date}</div>\n'
            f'  {as_paragraph(item.get("description"))}\n'
            f'  {bullets_html}\n'
            '</div>'
        )
    return section(SECTION_TITLES[language_code]["projects"], "\n".join(blocks))


def render_personal_info(bundle: dict[str, Any], profile: dict[str, Any], language_code: str) -> str:
    """Render localized Personal Information.

    DEC-071: Prefer reviewed AI response data from generation_response_personal.
    Fallbacks are intentionally kept for prototype/debug safety:
    - profile contact/additional data for old DBs
    - normalized profile work_formats from DEC-022 if AI returned null
    """
    labels = PERSONAL_LABELS[language_code]
    personal = (
        bundle.get("personal")
        or bundle.get("personal_info")
        or bundle.get("generation_response_personal")
        or {}
    )
    contact = profile.get("contact") or {}
    additional = profile.get("additional") or {}

    location = first_non_empty(personal.get("location"), contact.get("location"))
    spoken_languages = first_non_empty(personal.get("spoken_languages"), additional.get("languages"))
    relocation = first_non_empty(
        personal.get("willingness_to_relocate"),
        bool_or_text(additional.get("ready_for_relocation"), language_code),
    )
    business_trips = first_non_empty(
        personal.get("willingness_for_business_trips"),
        bool_or_text(additional.get("ready_for_business_trips"), language_code),
    )
    work_formats = first_non_empty(
        personal.get("work_formats"),
        localized_profile_work_formats(profile, language_code),
    )
    citizenship = first_non_empty(personal.get("citizenship"), additional.get("citizenship"))
    date_of_birth = first_non_empty(personal.get("date_of_birth"), additional.get("date_of_birth"))
    gpa_grade = first_non_empty(personal.get("gpa_grade"), profile_level_gpa(profile))

    items: list[tuple[str, Any]] = []
    if location:
        items.append((labels["location"], location))
    if spoken_languages:
        items.append((labels["languages"], spoken_languages))
    if relocation:
        items.append((labels["relocation"], relocation))
    if business_trips:
        items.append((labels["businessTrips"], business_trips))
    if work_formats:
        items.append((labels["workFormat"], work_formats))
    if citizenship:
        items.append((labels["citizenship"], citizenship))
    if date_of_birth:
        items.append((labels["dateOfBirth"], date_of_birth))
    if gpa_grade:
        items.append((labels["gpaGrade"], gpa_grade))

    spans = " ".join(
        f'<span><strong>{esc(label)}:</strong> {esc(value)}</span>'
        for label, value in items
        if value is not None and str(value).strip() != ""
    )
    if not spans:
        return ""
    return (
        '<section class="compact-section">\n'
        f'  <div class="section-title">{esc(SECTION_TITLES[language_code]["personal"])}</div>\n'
        f'  <div class="compact-info">{spans}</div>\n'
        '</section>'
    )


def first_non_empty(*values: Any) -> Any:
    for value in values:
        if value is not None and str(value).strip() != "":
            return value
    return None


def localized_profile_work_formats(profile: dict[str, Any], language_code: str) -> str | None:
    work_formats = profile.get("work_formats") or {}
    key = "english" if language_code == "EN" else "russian"
    value = work_formats.get(key)
    if value:
        return str(value)
    list_key = "en" if language_code == "EN" else "ru"
    values = work_formats.get(list_key) or []
    if isinstance(values, list) and values:
        return ", ".join(str(v) for v in values if v)
    return None


def profile_level_gpa(profile: dict[str, Any]) -> str | None:
    for item in profile.get("education", []) or []:
        value = item.get("gpa_grade")
        if value:
            return str(value)
    return None


def bool_or_text(value: Any, language_code: str) -> str | None:
    if value is None:
        return None
    if isinstance(value, bool):
        return bool_text(value, language_code)
    if isinstance(value, int) and value in (0, 1):
        return bool_text(bool(value), language_code)
    text = str(value).strip()
    if text.lower() in {"true", "yes", "y", "1"}:
        return bool_text(True, language_code)
    if text.lower() in {"false", "no", "n", "0"}:
        return bool_text(False, language_code)
    return text


def build_fallback_resume_body(bundle: dict[str, Any], profile: dict[str, Any]) -> str:
    language_code = str(bundle["response"]["language_code"]).upper()
    return "\n".join([
        render_header(bundle, profile),
        render_summary(bundle, language_code),
        render_experience(bundle, language_code, primary=True),
        render_skills(bundle, language_code),
        render_education(profile, language_code),
        render_courses(bundle, language_code),
        render_projects(bundle, language_code),
        render_aspirations(bundle, language_code),
        render_experience(bundle, language_code, primary=False),
        render_personal_info(bundle, profile, language_code),
    ])


def section(title: str, inner_html: str) -> str:
    if not inner_html or not inner_html.strip():
        return ""
    return f'<section>\n  <div class="section-title">{esc(title)}</div>\n  {inner_html}\n</section>'


def as_paragraph(value: Any) -> str:
    if value is None or str(value).strip() == "":
        return ""
    # Keep safe limited HTML if AI response contains allowed tags, otherwise text is escaped.
    text = str(value)
    if "<" in text and ">" in text:
        return clean_html(text)
    return f"<p>{esc(text)}</p>"


def clean_html(value: str | None) -> str:
    if value is None:
        return ""
    text = str(value)
    text = re.sub(r"<\s*script\b[^>]*>.*?<\s*/\s*script\s*>", "", text, flags=re.I | re.S)
    text = re.sub(r"\s+on\w+\s*=\s*(['\"]).*?\1", "", text, flags=re.I | re.S)
    text = re.sub(r"\s+style\s*=\s*(['\"]).*?\1", "", text, flags=re.I | re.S)

    def replace_tag(match: re.Match[str]) -> str:
        slash, tag = match.group(1), match.group(2).lower()
        if tag not in ALLOWED_HTML_TAGS:
            return ""
        if tag == "br":
            return "<br>"
        return f"<{slash}{tag}>"

    return re.sub(r"<\s*(/?)\s*([a-zA-Z0-9]+)(?:\s+[^>]*)?>", replace_tag, text)


def year_range(start_date: Any, end_date: Any, language_code: str) -> str:
    return f"{year(start_date, language_code)}-{year(end_date, language_code)}"


def year(date_value: Any, language_code: str) -> str:
    if not date_value:
        return "Present" if language_code == "EN" else "н.в."
    text = str(date_value)
    return text[:4] if len(text) >= 4 else text


def bool_text(value: Any, language_code: str) -> str:
    yes = "Yes" if language_code == "EN" else "Да"
    no = "No" if language_code == "EN" else "Нет"
    if isinstance(value, str):
        lowered = value.strip().lower()
        if lowered in {"true", "yes", "y", "1", "да"}:
            return yes
        if lowered in {"false", "no", "n", "0", "нет"}:
            return no
    return yes if bool(value) else no


def join_non_empty(values: list[Any]) -> str:
    return " | ".join(str(v) for v in values if v)


def esc(value: Any) -> str:
    return html.escape("" if value is None else str(value), quote=True)
