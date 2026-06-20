package com.resumainer.pdfspike.model;

import java.util.ArrayList;
import java.util.List;

/**
 * SPIKE ONLY - DO NOT PORT AS PRODUCTION DATA FACTORY.
 * Generates synthetic resume content for edge-case validation.
 * In the capstone app, map approved generated resume data into ResumeData/ResumePdfData instead.
 */
public final class ResumeDataFactory {
    public ResumeData create(MockCandidate c, Language lang) {
        boolean ru = lang == Language.RU;
        String fullName = ru ? c.ruFullName() : c.enFullName();
        String title = ru ? c.ruTitle() : c.enTitle();
        String loc = ru ? c.ruLocation() : c.enLocation();
        return new ResumeData(
                lang,
                c.ecNumber(),
                fullName,
                title,
                c.phone(), c.email(), loc, c.linkedin(), c.portfolio(), c.telegram(), c.whatsapp(),
                ru ? "Java | Spring MVC | JDBC | REST API | Системный анализ | PostgreSQL" : "Java | Spring MVC | JDBC | REST API | Systems Analysis | PostgreSQL",
                ru ? ruSummary() : enSummary(),
                work(c.workCount(), ru),
                skills(ru),
                education(ru),
                courses(c.courseCount(), ru),
                projects(c.projectCount(), ru),
                ru ? ruAspirations() : enAspirations(),
                personal1(loc, ru),
                personal2(ru),
                ru ? "Формат работы: Гибрид / удаленно" : "Work Format: Hybrid / Remote"
        );
    }

    private String enSummary() {
        return "Experienced analyst-developer combining public-sector delivery, systems analysis, Java backend practice, and data reporting. " +
                "Works with stakeholders to clarify goals, document requirements, model processes, validate data, and coordinate delivery. " +
                "Comfortable turning vague business needs into tested implementation-ready tasks.";
    }
    private String ruSummary() {
        return "Аналитик-разработчик с опытом цифровой поставки, системного анализа, Java backend практики и отчетности. " +
                "Работает со стейкхолдерами, уточняет цели, описывает требования, моделирует процессы, проверяет данные и координирует поставку. " +
                "Умеет превращать нечеткие бизнес-запросы в проверяемые задачи для реализации.";
    }

    private List<WorkExperience> work(int count, boolean ru) {
        List<WorkExperience> items = new ArrayList<>();
        String[] enRoles = {"Senior Business Analyst", "Systems Analyst", "Business Analyst", "Requirements Analyst", "Product Analyst", "Data Analyst", "Translation Coordinator", "Support Analyst"};
        String[] ruRoles = {"Старший бизнес-аналитик", "Системный аналитик", "Бизнес-аналитик", "Аналитик требований", "Продуктовый аналитик", "Дата-аналитик", "Координатор переводов", "Аналитик поддержки"};
        String[] companies = {"Digital Government Solutions", "GovTech Integration Lab", "Bobrosoft", "Regional Automation Office", "Public Services Lab", "Reporting Center", "Operations Bureau", "Helpdesk Group"};
        String[] starts = {"2025-01", "2024-02", "2023-03", "2022-05", "2021-07", "2020-08", "2018-01", "2017-01"};
        String[] ends = {ru ? "по настоящее время" : "till now", "2024-12", "2023-11", "2023-02", "2022-04", "2021-06", "2020-07", "2017-12"};
        for (int i = 0; i < count; i++) {
            boolean primary = i < 3;
            int sentenceCount = count == 1 ? 5 : (primary ? 3 : 1);
            int bulletCount = primary ? (count == 1 ? 9 : 5) : 0;
            items.add(new WorkExperience(
                    ru ? ruRoles[i] : enRoles[i], companies[i], ru ? "Астана, Казахстан" : "Astana, Kazakhstan", starts[i], ends[i],
                    description(sentenceCount, ru), bullets(bulletCount, ru)
            ));
        }
        return items;
    }

    private String description(int sentences, boolean ru) {
        String[] en = {
                "Led requirements discovery, process modeling, and delivery coordination for digital services.",
                "Prepared stakeholder-ready specifications and clarified implementation details with developers.",
                "Supported acceptance testing, release decisions, and risk tracking during delivery.",
                "Improved traceability between business goals, user stories, data rules, and test scenarios.",
                "Documented integration assumptions and helped reduce rework during implementation."
        };
        String[] ruS = {
                "Выполнял сбор требований, моделирование процессов и координацию поставки цифровых сервисов.",
                "Готовил спецификации для стейкхолдеров и уточнял детали реализации с разработчиками.",
                "Поддерживал приемочное тестирование, решения по релизам и отслеживание рисков.",
                "Улучшал прослеживаемость между бизнес-целями, user stories, правилами данных и тестами.",
                "Документировал интеграционные допущения и помогал снижать переработки при реализации."
        };
        StringBuilder sb = new StringBuilder();
        String[] arr = ru ? ruS : en;
        for (int i = 0; i < sentences && i < arr.length; i++) {
            if (i > 0) sb.append(' ');
            sb.append(arr[i]);
        }
        return sb.toString();
    }

    private List<String> bullets(int count, boolean ru) {
        String[] en = {
                "Defined acceptance criteria for cross-functional delivery.",
                "Mapped workflows and clarified API requirements.",
                "Coordinated review cycles with product and engineering.",
                "Validated reporting data against stakeholder expectations.",
                "Prepared diagrams and implementation notes.",
                "Reduced ambiguity in backlog refinement sessions.",
                "Supported QA with business test scenarios.",
                "Tracked scope changes and dependency risks.",
                "Improved documentation handoff for developers."
        };
        String[] ruS = {
                "Формировал критерии приемки для кросс-функциональной поставки.",
                "Моделировал процессы и уточнял требования к API.",
                "Координировал циклы ревью с продуктовой и инженерной командами.",
                "Проверял отчетные данные относительно ожиданий стейкхолдеров.",
                "Готовил диаграммы и заметки для реализации.",
                "Снижaл неоднозначность на сессиях уточнения backlog.",
                "Поддерживал QA бизнес-сценариями тестирования.",
                "Отслеживал изменения scope и риски зависимостей.",
                "Улучшал передачу документации разработчикам."
        };
        List<String> out = new ArrayList<>();
        String[] arr = ru ? ruS : en;
        for (int i = 0; i < count && i < arr.length; i++) out.add(arr[i]);
        return out;
    }

    private List<ProjectItem> projects(int count, boolean ru) {
        List<ProjectItem> list = new ArrayList<>();
        String[] enTitles = {"Resume Generator Capstone", "Cashflow Web App", "Analytics Dashboard"};
        String[] ruTitles = {"Capstone генератор резюме", "Веб-приложение Cashflow", "Аналитический дашборд"};
        for (int i = 0; i < count; i++) {
            list.add(new ProjectItem(ru ? ruTitles[i] : enTitles[i], ru ? "Разработчик / аналитик" : "Developer / Analyst", ru ? "Удаленно" : "Remote", "2026-01", "2026-06", projectDescription(ru), projectBullets(ru)));
        }
        return list;
    }
    private String projectDescription(boolean ru) {
        return ru ? "Создал структурированный workflow резюме с AI-генерацией и review. Спроектировал validation и export flow для надежной поставки. Подготовил прототип для безопасного внедрения." :
                "Built a structured resume workflow with AI-assisted generation and review. Designed validation and export flow for reliable delivery. Prepared a prototype for safe implementation.";
    }
    private List<String> projectBullets(boolean ru) {
        return ru ? List.of("Спроектировал модель данных и pipeline рендера.", "Реализовал validation, тесты и export flow.", "Подготовил документацию для безопасной реализации.", "Проверил edge cases на тестовых данных.") :
                List.of("Designed data model and rendering pipeline.", "Implemented validation, tests, and export flow.", "Prepared documentation for safe implementation.", "Verified edge cases with test data.");
    }

    private List<SkillGroup> skills(boolean ru) {
        return ru ? List.of(
                new SkillGroup("Бизнес- и системный анализ", List.of("Требования", "BPMN", "User Stories", "критерии приемки", "SQL", "RACI")),
                new SkillGroup("Java и backend", List.of("Java", "Spring MVC", "JDBC", "REST API", "PostgreSQL", "JUnit")),
                new SkillGroup("Данные и отчетность", List.of("Excel", "Power BI", "дашборды", "отчеты", "проверка данных")),
                new SkillGroup("Тестирование и delivery", List.of("Mockito", "GitHub Actions", "Docker", "acceptance", "debugging")),
                new SkillGroup("Коммуникация", List.of("стейкхолдеры", "фасилитация", "документация", "handoff", "риски"))) :
                List.of(
                        new SkillGroup("Business & Systems Analysis", List.of("Requirements", "BPMN", "User Stories", "Acceptance Criteria", "SQL", "RACI")),
                        new SkillGroup("Java & Backend", List.of("Java", "Spring MVC", "JDBC", "REST API", "PostgreSQL", "JUnit")),
                        new SkillGroup("Data & Reporting", List.of("Excel", "Power BI", "Dashboards", "Reporting", "Data Validation")),
                        new SkillGroup("Testing & Delivery", List.of("Mockito", "GitHub Actions", "Docker", "Acceptance", "Debugging")),
                        new SkillGroup("Communication", List.of("Stakeholders", "Facilitation", "Documentation", "Handoff", "Risks")));
    }

    private List<String> education(boolean ru) {
        return ru ? List.of("Бакалавр: Информационные системы | КАФУ", "Бакалавр: Переводческое дело | Евразийский гуманитарный институт", "MBA: Управление IT-проектами | Институт проектного управления", "Магистр: Бизнес-аналитика | Центр профессиональной аналитики") :
                List.of("Bachelor: Information Systems | KAFU", "Bachelor: Translation Studies | Eurasian Humanities Institute", "MBA: IT Project Management | Institute of Project Management", "Master: Business Analytics | Professional Analytics Center");
    }

    private List<CourseItem> courses(int count, boolean ru) {
        String[][] en = {{"Business Analysis Foundations","LinkedIn Learning","requirements, stakeholders, scope"},{"Java Backend Development","LinkedIn Learning","Java, API, testing"},{"SQL for Data Analysis","LinkedIn Learning","queries, joins, reporting"},{"Docker Essentials","LinkedIn Learning","containers, images, deployment"},{"Power BI Reporting","LinkedIn Learning","dashboards, data, visuals"}};
        String[][] ruS = {{"Основы бизнес-анализа","LinkedIn Learning","требования, стейкхолдеры, scope"},{"Java Backend Development","LinkedIn Learning","Java, API, тестирование"},{"SQL для анализа данных","LinkedIn Learning","запросы, join, отчеты"},{"Docker Essentials","LinkedIn Learning","контейнеры, образы, деплой"},{"Power BI Reporting","LinkedIn Learning","дашборды, данные, визуализация"}};
        List<CourseItem> list = new ArrayList<>();
        String[][] arr = ru ? ruS : en;
        for (int i = 0; i < count && i < arr.length; i++) list.add(new CourseItem(arr[i][0], arr[i][1], i == 3 ? "" : arr[i][2]));
        return list;
    }

    private String enAspirations() {
        return "I want to grow into a backend-focused analyst-developer role. I am interested in systems where business rules, data quality, and user workflows meet. My next step is to strengthen Java backend delivery while keeping strong analysis discipline. I prefer teams that value clear requirements, reliable tests, and practical documentation. Long term, I want to design maintainable digital products that reduce manual work. I also want to improve architecture decisions, API design, and database modeling. A strong environment for me includes feedback, ownership, and measurable delivery. I am motivated by useful tools, automation, and clear product outcomes. This direction combines my analysis background with hands-on engineering growth.";
    }
    private String ruAspirations() {
        return "Я хочу развиваться в роли аналитика-разработчика с фокусом на backend. Мне интересны системы, где пересекаются бизнес-правила, качество данных и пользовательские процессы. Следующий шаг — усилить Java backend при сохранении сильной аналитической дисциплины. Мне близки команды, где ценят ясные требования, надежные тесты и практичную документацию. В долгосрочной перспективе хочу проектировать поддерживаемые цифровые продукты, которые уменьшают ручной труд. Также хочу развивать архитектурные решения, проектирование API и моделирование баз данных. Сильная среда для меня включает обратную связь, ownership и измеримую поставку. Меня мотивируют полезные инструменты, автоматизация и понятные продуктовые результаты. Это направление объединяет мой аналитический опыт с практическим инженерным ростом.";
    }
    private String personal1(String loc, boolean ru) {
        return ru ? "Местоположение: " + loc + " | Гражданство: Казахстан | Релокация: Готов к релокации | Командировки: Готов" :
                "Location: " + loc + " | Citizenship: Kazakhstan | Relocation: Ready to relocate | Business Trips: Available";
    }
    private String personal2(boolean ru) {
        return ru ? "Языки: Русский - родной, Английский - B2 | Дата рождения: 1993-04-15" :
                "Languages: English - B2, Russian - native | Date of Birth: 1993-04-15";
    }
}
