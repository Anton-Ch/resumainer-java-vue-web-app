Project Path: prototype

Source Tree:

```txt
prototype
├── code2prompt.exe
├── index.html
├── mq3luiby-package.json
├── mq3luil5-package-lock.json
├── package-lock.json
├── package.json
├── src
│   ├── App.vue
│   ├── assets
│   │   └── styles
│   │       └── vue_general.css
│   ├── components
│   │   ├── AppHeader.vue
│   │   ├── LanguageSwitcher.vue
│   │   ├── common
│   │   │   ├── GeneratePlaceholderPage.vue
│   │   │   └── ProfilePlaceholderPage.vue
│   │   ├── home
│   │   │   ├── GuidedNextStep.vue
│   │   │   ├── ResumeDetailsDialog.vue
│   │   │   ├── SavedResumesTable.vue
│   │   │   └── SummaryCards.vue
│   │   └── profile
│   │       ├── EmptyRecordsState.vue
│   │       ├── InlineRecordForm.vue
│   │       ├── ProfileMobileTabs.vue
│   │       ├── ProfileSectionHeader.vue
│   │       ├── ProfileShell.vue
│   │       ├── ProfileSidebar.vue
│   │       ├── RecordCard.vue
│   │       ├── UnsavedChangesDialog.vue
│   │       ├── courses
│   │       │   ├── CourseDialog.vue
│   │       │   └── CoursesTable.vue
│   │       └── sections
│   │           ├── AdditionalInfoSection.vue
│   │           ├── ContactDetailsSection.vue
│   │           ├── CoursesSection.vue
│   │           ├── EducationSection.vue
│   │           ├── ProjectsSection.vue
│   │           └── WorkExperienceSection.vue
│   ├── composables
│   │   ├── useAuth.ts
│   │   └── useUserHome.ts
│   ├── env.d.ts
│   ├── i18n
│   │   ├── en.json
│   │   ├── index.ts
│   │   └── ru.json
│   ├── main.ts
│   ├── router
│   │   └── index.ts
│   ├── services
│   │   ├── authService.ts
│   │   ├── profileMockService.ts
│   │   ├── resumeService.ts
│   │   └── userHomeService.ts
│   ├── types
│   │   └── profile.ts
│   └── views
│       ├── AdminHomePage.vue
│       ├── AuthPage.vue
│       ├── ProfilePage.vue
│       └── UserHomePage.vue
├── tsconfig.json
├── tsconfig.node.json
└── vite.config.ts

```

`index.html`:

```html
<!doctype html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>ResumAIner</title>
    <link rel="preconnect" href="https://fonts.googleapis.com" />
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Manrope:wght@600;700;800&display=swap" rel="stylesheet" />
  </head>
  <body>
    <div id="app"></div>
    <script type="module" src="/src/main.ts"></script>
  </body>
</html>

```

`mq3luiby-package.json`:

```json
{
  "name": "resumainer-frontend",
  "version": "0.1.0",
  "private": true,
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vue-tsc --noEmit && vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "@primeuix/themes": "^1.0.2",
    "@primevue/forms": "^4.3.3",
    "primeicons": "^7.0.0",
    "primevue": "^4.3.3",
    "vue": "^3.5.13",
    "vue-i18n": "^10.0.7",
    "vue-router": "^4.5.0",
    "zod": "^3.24.3"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.2.3",
    "typescript": "~5.8.3",
    "vite": "^6.3.5",
    "vue-tsc": "^2.2.8"
  }
}

```

`mq3luil5-package-lock.json`:

```json
{
  "name": "resumainer-frontend",
  "version": "0.1.0",
  "lockfileVersion": 3,
  "requires": true,
  "packages": {
    "": {
      "name": "resumainer-frontend",
      "version": "0.1.0",
      "dependencies": {
        "@primeuix/themes": "^1.0.2",
        "@primevue/forms": "^4.3.3",
        "primeicons": "^7.0.0",
        "primevue": "^4.3.3",
        "vue": "^3.5.13",
        "vue-i18n": "^10.0.7",
        "vue-router": "^4.5.0",
        "zod": "^3.24.3"
      },
      "devDependencies": {
        "@vitejs/plugin-vue": "^5.2.3",
        "typescript": "~5.8.3",
        "vite": "^6.3.5",
        "vue-tsc": "^2.2.8"
      }
    },
    "node_modules/@babel/helper-string-parser": {
      "version": "7.29.7",
      "resolved": "https://registry.npmjs.org/@babel/helper-string-parser/-/helper-string-parser-7.29.7.tgz",
      "integrity": "sha512-Pb5ijPrZ89GDH8223L4UP8i6QApWxs04RbPQJTeWDV0/keR2E36MeKnyr6LYmUUvqRRI+Iv87SuF1W6ErINzYw==",
      "license": "MIT",
      "engines": {
        "node": ">=6.9.0"
      }
    },
    "node_modules/@babel/helper-validator-identifier": {
      "version": "7.29.7",
      "resolved": "https://registry.npmjs.org/@babel/helper-validator-identifier/-/helper-validator-identifier-7.29.7.tgz",
      "integrity": "sha512-qehxGkRj55h/ff8EMaJ+cYhyaKlHIxqYDn682wQD7RNp9UujOQsHog2uS0r2vzr4pW+sXf90NeeayjcNaX3fFg==",
      "license": "MIT",
      "engines": {
        "node": ">=6.9.0"
      }
    },
    "node_modules/@babel/parser": {
      "version": "7.29.7",
      "resolved": "https://registry.npmjs.org/@babel/parser/-/parser-7.29.7.tgz",
      "integrity": "sha512-hnORnjP/1P/zFEndoeX+n+t1RwWRJiJpM/jO7FW32Kn9r5+sJB2JWOdYo4L6k78j15eCwY3Gm/7364B1EMwtNg==",
      "license": "MIT",
      "dependencies": {
        "@babel/types": "^7.29.7"
      },
      "bin": {
        "parser": "bin/babel-parser.js"
      },
      "engines": {
        "node": ">=6.0.0"
      }
    },
    "node_modules/@babel/types": {
      "version": "7.29.7",
      "resolved": "https://registry.npmjs.org/@babel/types/-/types-7.29.7.tgz",
      "integrity": "sha512-4zBIxpPzowiZpusoFkyGVwakdRJUyuH5PxQ/PrqghfdFWWasvnCdPfQXHrenDai+gyLARulZjZowCOj6fjT4pA==",
      "license": "MIT",
      "dependencies": {
        "@babel/helper-string-parser": "^7.29.7",
        "@babel/helper-validator-identifier": "^7.29.7"
      },
      "engines": {
        "node": ">=6.9.0"
      }
    },
    "node_modules/@esbuild/aix-ppc64": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/aix-ppc64/-/aix-ppc64-0.25.12.tgz",
      "integrity": "sha512-Hhmwd6CInZ3dwpuGTF8fJG6yoWmsToE+vYgD4nytZVxcu1ulHpUQRAB1UJ8+N1Am3Mz4+xOByoQoSZf4D+CpkA==",
      "cpu": [
        "ppc64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "aix"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/android-arm": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/android-arm/-/android-arm-0.25.12.tgz",
      "integrity": "sha512-VJ+sKvNA/GE7Ccacc9Cha7bpS8nyzVv0jdVgwNDaR4gDMC/2TTRc33Ip8qrNYUcpkOHUT5OZ0bUcNNVZQ9RLlg==",
      "cpu": [
        "arm"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "android"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/android-arm64": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/android-arm64/-/android-arm64-0.25.12.tgz",
      "integrity": "sha512-6AAmLG7zwD1Z159jCKPvAxZd4y/VTO0VkprYy+3N2FtJ8+BQWFXU+OxARIwA46c5tdD9SsKGZ/1ocqBS/gAKHg==",
      "cpu": [
        "arm64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "android"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/android-x64": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/android-x64/-/android-x64-0.25.12.tgz",
      "integrity": "sha512-5jbb+2hhDHx5phYR2By8GTWEzn6I9UqR11Kwf22iKbNpYrsmRB18aX/9ivc5cabcUiAT/wM+YIZ6SG9QO6a8kg==",
      "cpu": [
        "x64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "android"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/darwin-arm64": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/darwin-arm64/-/darwin-arm64-0.25.12.tgz",
      "integrity": "sha512-N3zl+lxHCifgIlcMUP5016ESkeQjLj/959RxxNYIthIg+CQHInujFuXeWbWMgnTo4cp5XVHqFPmpyu9J65C1Yg==",
      "cpu": [
        "arm64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "darwin"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/darwin-x64": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/darwin-x64/-/darwin-x64-0.25.12.tgz",
      "integrity": "sha512-HQ9ka4Kx21qHXwtlTUVbKJOAnmG1ipXhdWTmNXiPzPfWKpXqASVcWdnf2bnL73wgjNrFXAa3yYvBSd9pzfEIpA==",
      "cpu": [
        "x64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "darwin"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/freebsd-arm64": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/freebsd-arm64/-/freebsd-arm64-0.25.12.tgz",
      "integrity": "sha512-gA0Bx759+7Jve03K1S0vkOu5Lg/85dou3EseOGUes8flVOGxbhDDh/iZaoek11Y8mtyKPGF3vP8XhnkDEAmzeg==",
      "cpu": [
        "arm64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "freebsd"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/freebsd-x64": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/freebsd-x64/-/freebsd-x64-0.25.12.tgz",
      "integrity": "sha512-TGbO26Yw2xsHzxtbVFGEXBFH0FRAP7gtcPE7P5yP7wGy7cXK2oO7RyOhL5NLiqTlBh47XhmIUXuGciXEqYFfBQ==",
      "cpu": [
        "x64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "freebsd"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/linux-arm": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/linux-arm/-/linux-arm-0.25.12.tgz",
      "integrity": "sha512-lPDGyC1JPDou8kGcywY0YILzWlhhnRjdof3UlcoqYmS9El818LLfJJc3PXXgZHrHCAKs/Z2SeZtDJr5MrkxtOw==",
      "cpu": [
        "arm"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "linux"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/linux-arm64": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/linux-arm64/-/linux-arm64-0.25.12.tgz",
      "integrity": "sha512-8bwX7a8FghIgrupcxb4aUmYDLp8pX06rGh5HqDT7bB+8Rdells6mHvrFHHW2JAOPZUbnjUpKTLg6ECyzvas2AQ==",
      "cpu": [
        "arm64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "linux"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/linux-ia32": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/linux-ia32/-/linux-ia32-0.25.12.tgz",
      "integrity": "sha512-0y9KrdVnbMM2/vG8KfU0byhUN+EFCny9+8g202gYqSSVMonbsCfLjUO+rCci7pM0WBEtz+oK/PIwHkzxkyharA==",
      "cpu": [
        "ia32"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "linux"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/linux-loong64": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/linux-loong64/-/linux-loong64-0.25.12.tgz",
      "integrity": "sha512-h///Lr5a9rib/v1GGqXVGzjL4TMvVTv+s1DPoxQdz7l/AYv6LDSxdIwzxkrPW438oUXiDtwM10o9PmwS/6Z0Ng==",
      "cpu": [
        "loong64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "linux"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/linux-mips64el": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/linux-mips64el/-/linux-mips64el-0.25.12.tgz",
      "integrity": "sha512-iyRrM1Pzy9GFMDLsXn1iHUm18nhKnNMWscjmp4+hpafcZjrr2WbT//d20xaGljXDBYHqRcl8HnxbX6uaA/eGVw==",
      "cpu": [
        "mips64el"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "linux"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/linux-ppc64": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/linux-ppc64/-/linux-ppc64-0.25.12.tgz",
      "integrity": "sha512-9meM/lRXxMi5PSUqEXRCtVjEZBGwB7P/D4yT8UG/mwIdze2aV4Vo6U5gD3+RsoHXKkHCfSxZKzmDssVlRj1QQA==",
      "cpu": [
        "ppc64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "linux"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/linux-riscv64": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/linux-riscv64/-/linux-riscv64-0.25.12.tgz",
      "integrity": "sha512-Zr7KR4hgKUpWAwb1f3o5ygT04MzqVrGEGXGLnj15YQDJErYu/BGg+wmFlIDOdJp0PmB0lLvxFIOXZgFRrdjR0w==",
      "cpu": [
        "riscv64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "linux"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/linux-s390x": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/linux-s390x/-/linux-s390x-0.25.12.tgz",
      "integrity": "sha512-MsKncOcgTNvdtiISc/jZs/Zf8d0cl/t3gYWX8J9ubBnVOwlk65UIEEvgBORTiljloIWnBzLs4qhzPkJcitIzIg==",
      "cpu": [
        "s390x"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "linux"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/linux-x64": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/linux-x64/-/linux-x64-0.25.12.tgz",
      "integrity": "sha512-uqZMTLr/zR/ed4jIGnwSLkaHmPjOjJvnm6TVVitAa08SLS9Z0VM8wIRx7gWbJB5/J54YuIMInDquWyYvQLZkgw==",
      "cpu": [
        "x64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "linux"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/netbsd-arm64": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/netbsd-arm64/-/netbsd-arm64-0.25.12.tgz",
      "integrity": "sha512-xXwcTq4GhRM7J9A8Gv5boanHhRa/Q9KLVmcyXHCTaM4wKfIpWkdXiMog/KsnxzJ0A1+nD+zoecuzqPmCRyBGjg==",
      "cpu": [
        "arm64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "netbsd"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/netbsd-x64": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/netbsd-x64/-/netbsd-x64-0.25.12.tgz",
      "integrity": "sha512-Ld5pTlzPy3YwGec4OuHh1aCVCRvOXdH8DgRjfDy/oumVovmuSzWfnSJg+VtakB9Cm0gxNO9BzWkj6mtO1FMXkQ==",
      "cpu": [
        "x64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "netbsd"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/openbsd-arm64": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/openbsd-arm64/-/openbsd-arm64-0.25.12.tgz",
      "integrity": "sha512-fF96T6KsBo/pkQI950FARU9apGNTSlZGsv1jZBAlcLL1MLjLNIWPBkj5NlSz8aAzYKg+eNqknrUJ24QBybeR5A==",
      "cpu": [
        "arm64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "openbsd"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/openbsd-x64": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/openbsd-x64/-/openbsd-x64-0.25.12.tgz",
      "integrity": "sha512-MZyXUkZHjQxUvzK7rN8DJ3SRmrVrke8ZyRusHlP+kuwqTcfWLyqMOE3sScPPyeIXN/mDJIfGXvcMqCgYKekoQw==",
      "cpu": [
        "x64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "openbsd"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/openharmony-arm64": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/openharmony-arm64/-/openharmony-arm64-0.25.12.tgz",
      "integrity": "sha512-rm0YWsqUSRrjncSXGA7Zv78Nbnw4XL6/dzr20cyrQf7ZmRcsovpcRBdhD43Nuk3y7XIoW2OxMVvwuRvk9XdASg==",
      "cpu": [
        "arm64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "openharmony"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/sunos-x64": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/sunos-x64/-/sunos-x64-0.25.12.tgz",
      "integrity": "sha512-3wGSCDyuTHQUzt0nV7bocDy72r2lI33QL3gkDNGkod22EsYl04sMf0qLb8luNKTOmgF/eDEDP5BFNwoBKH441w==",
      "cpu": [
        "x64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "sunos"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/win32-arm64": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/win32-arm64/-/win32-arm64-0.25.12.tgz",
      "integrity": "sha512-rMmLrur64A7+DKlnSuwqUdRKyd3UE7oPJZmnljqEptesKM8wx9J8gx5u0+9Pq0fQQW8vqeKebwNXdfOyP+8Bsg==",
      "cpu": [
        "arm64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "win32"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/win32-ia32": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/win32-ia32/-/win32-ia32-0.25.12.tgz",
      "integrity": "sha512-HkqnmmBoCbCwxUKKNPBixiWDGCpQGVsrQfJoVGYLPT41XWF8lHuE5N6WhVia2n4o5QK5M4tYr21827fNhi4byQ==",
      "cpu": [
        "ia32"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "win32"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/win32-x64": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/win32-x64/-/win32-x64-0.25.12.tgz",
      "integrity": "sha512-alJC0uCZpTFrSL0CCDjcgleBXPnCrEAhTBILpeAp7M/OFgoqtAetfBzX0xM00MUsVVPpVjlPuMbREqnZCXaTnA==",
      "cpu": [
        "x64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "win32"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@intlify/core-base": {
      "version": "10.0.8",
      "resolved": "https://registry.npmjs.org/@intlify/core-base/-/core-base-10.0.8.tgz",
      "integrity": "sha512-FoHslNWSoHjdUBLy35bpm9PV/0LVI/DSv9L6Km6J2ad8r/mm0VaGg06C40FqlE8u2ADcGUM60lyoU7Myo4WNZQ==",
      "license": "MIT",
      "dependencies": {
        "@intlify/message-compiler": "10.0.8",
        "@intlify/shared": "10.0.8"
      },
      "engines": {
        "node": ">= 16"
      },
      "funding": {
        "url": "https://github.com/sponsors/kazupon"
      }
    },
    "node_modules/@intlify/message-compiler": {
      "version": "10.0.8",
      "resolved": "https://registry.npmjs.org/@intlify/message-compiler/-/message-compiler-10.0.8.tgz",
      "integrity": "sha512-DV+sYXIkHVd5yVb2mL7br/NEUwzUoLBsMkV3H0InefWgmYa34NLZUvMCGi5oWX+Hqr2Y2qUxnVrnOWF4aBlgWg==",
      "license": "MIT",
      "dependencies": {
        "@intlify/shared": "10.0.8",
        "source-map-js": "^1.0.2"
      },
      "engines": {
        "node": ">= 16"
      },
      "funding": {
        "url": "https://github.com/sponsors/kazupon"
      }
    },
    "node_modules/@intlify/shared": {
      "version": "10.0.8",
      "resolved": "https://registry.npmjs.org/@intlify/shared/-/shared-10.0.8.tgz",
      "integrity": "sha512-BcmHpb5bQyeVNrptC3UhzpBZB/YHHDoEREOUERrmF2BRxsyOEuRrq+Z96C/D4+2KJb8kuHiouzAei7BXlG0YYw==",
      "license": "MIT",
      "engines": {
        "node": ">= 16"
      },
      "funding": {
        "url": "https://github.com/sponsors/kazupon"
      }
    },
    "node_modules/@jridgewell/sourcemap-codec": {
      "version": "1.5.5",
      "resolved": "https://registry.npmjs.org/@jridgewell/sourcemap-codec/-/sourcemap-codec-1.5.5.tgz",
      "integrity": "sha512-cYQ9310grqxueWbl+WuIUIaiUaDcj7WOq5fVhEljNVgRfOUhY9fy2zTvfoqWsnebh8Sl70VScFbICvJnLKB0Og==",
      "license": "MIT"
    },
    "node_modules/@primeuix/forms": {
      "version": "0.1.0",
      "resolved": "https://registry.npmjs.org/@primeuix/forms/-/forms-0.1.0.tgz",
      "integrity": "sha512-LctcQidb+B5PuvAFWH24YH/SIzmHlOabLHpaTeGY/k51iBv1WyCp+5w9JMYuMB/BplSvV0ZGySxQVkN5Azr/aQ==",
      "license": "MIT",
      "dependencies": {
        "@primeuix/utils": "^0.6.0"
      },
      "engines": {
        "node": ">=12.11.0"
      }
    },
    "node_modules/@primeuix/styled": {
      "version": "0.7.4",
      "resolved": "https://registry.npmjs.org/@primeuix/styled/-/styled-0.7.4.tgz",
      "integrity": "sha512-QSO/NpOQg8e9BONWRBx9y8VGMCMYz0J/uKfNJEya/RGEu7ARx0oYW0ugI1N3/KB1AAvyGxzKBzGImbwg0KUiOQ==",
      "license": "MIT",
      "dependencies": {
        "@primeuix/utils": "^0.6.1"
      },
      "engines": {
        "node": ">=12.11.0"
      }
    },
    "node_modules/@primeuix/styles": {
      "version": "2.0.3",
      "resolved": "https://registry.npmjs.org/@primeuix/styles/-/styles-2.0.3.tgz",
      "integrity": "sha512-2ykAB6BaHzR/6TwF8ShpJTsZrid6cVIEBVlookSdvOdmlWuevGu5vWOScgIwqWwlZcvkFYAGR/SUV3OHCTBMdw==",
      "license": "MIT",
      "dependencies": {
        "@primeuix/styled": "^0.7.4"
      }
    },
    "node_modules/@primeuix/themes": {
      "version": "1.2.5",
      "resolved": "https://registry.npmjs.org/@primeuix/themes/-/themes-1.2.5.tgz",
      "integrity": "sha512-n3YkwJrHQaEESc/D/A/iD815sxp8cKnmzscA6a8Tm8YvMtYU32eCahwLLe6h5rywghVwxASWuG36XBgISYOIjQ==",
      "license": "MIT",
      "dependencies": {
        "@primeuix/styled": "^0.7.3"
      }
    },
    "node_modules/@primeuix/utils": {
      "version": "0.6.4",
      "resolved": "https://registry.npmjs.org/@primeuix/utils/-/utils-0.6.4.tgz",
      "integrity": "sha512-pZ5f+vj7wSzRhC7KoEQRU5fvYAe+RP9+m39CTscZ3UywCD1Y2o6Fe1rRgklMPSkzUcty2jzkA0zMYkiJBD1hgg==",
      "license": "MIT",
      "engines": {
        "node": ">=12.11.0"
      }
    },
    "node_modules/@primevue/core": {
      "version": "4.5.5",
      "resolved": "https://registry.npmjs.org/@primevue/core/-/core-4.5.5.tgz",
      "integrity": "sha512-JpkXhq1ddc70JdsC3CC4dM+UbeeWuCW/8DpS9dNBfrOk824TLSlRlMEGFyVKqRMn5WPQvYLiy3xXfLQeNdSqhQ==",
      "license": "MIT",
      "dependencies": {
        "@primeuix/styled": "^0.7.4",
        "@primeuix/utils": "^0.6.2"
      },
      "engines": {
        "node": ">=12.11.0"
      },
      "peerDependencies": {
        "vue": "^3.5.0"
      }
    },
    "node_modules/@primevue/forms": {
      "version": "4.5.5",
      "resolved": "https://registry.npmjs.org/@primevue/forms/-/forms-4.5.5.tgz",
      "integrity": "sha512-LUeIt6oItwCZyBreQ7ycErE8aEjPmBWOTq1VPLhyaATcnzMBOZRIiwclaYk8s/tf5VYMqN9N4B80XnXoO7OdvQ==",
      "license": "MIT",
      "dependencies": {
        "@primeuix/forms": "^0.1.0",
        "@primeuix/utils": "^0.6.2",
        "@primevue/core": "4.5.5"
      },
      "engines": {
        "node": ">=12.11.0"
      }
    },
    "node_modules/@primevue/icons": {
      "version": "4.5.5",
      "resolved": "https://registry.npmjs.org/@primevue/icons/-/icons-4.5.5.tgz",
      "integrity": "sha512-eteOhTdAOXEYE9qW1AOrBBgDxQ2szHJxSkEK1XVdV2TKxGM5FQf03Ovms0VDyZTc16XBIgvwYjXJQS0BPbhPaA==",
      "license": "MIT",
      "dependencies": {
        "@primeuix/utils": "^0.6.2",
        "@primevue/core": "4.5.5"
      },
      "engines": {
        "node": ">=12.11.0"
      }
    },
    "node_modules/@rollup/rollup-android-arm-eabi": {
      "version": "4.61.0",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-android-arm-eabi/-/rollup-android-arm-eabi-4.61.0.tgz",
      "integrity": "sha512-dnxczajOqt0gesZlN5pGQ1s1imQVrsmCw5G2Ci4oM+0WvNz3pyRnlWrT7McoZIb8VlFwCawdmbWRmxRn7HI+VQ==",
      "cpu": [
        "arm"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "android"
      ]
    },
    "node_modules/@rollup/rollup-android-arm64": {
      "version": "4.61.0",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-android-arm64/-/rollup-android-arm64-4.61.0.tgz",
      "integrity": "sha512-Bp3JpGP00Vu3f238ivRrjf7z3xSzVPXqCmaJYA9t2c+c8vKYvOzmXF7LkkeUalTEGd6cZcSWe+PFIP3Vy48fRg==",
      "cpu": [
        "arm64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "android"
      ]
    },
    "node_modules/@rollup/rollup-darwin-arm64": {
      "version": "4.61.0",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-darwin-arm64/-/rollup-darwin-arm64-4.61.0.tgz",
      "integrity": "sha512-zaYIpr670mUmmZ1tVzUFplbQbG7h3Gugx3L5FoqhsC2m/YnLlR1a7zVLmXNPy+iY1tFPEbNG+HHBXZGyId0G5w==",
      "cpu": [
        "arm64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "darwin"
      ]
    },
    "node_modules/@rollup/rollup-darwin-x64": {
      "version": "4.61.0",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-darwin-x64/-/rollup-darwin-x64-4.61.0.tgz",
      "integrity": "sha512-+P49fvkv2dSoeevUW+lgZ/I2JHSsJCK1Lyjj7Cu6E4UHG4tS9XIefzIjo5qhgELjAclnen1rLzK2PMKJdo+Dyg==",
      "cpu": [
        "x64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "darwin"
      ]
    },
    "node_modules/@rollup/rollup-freebsd-arm64": {
      "version": "4.61.0",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-freebsd-arm64/-/rollup-freebsd-arm64-4.61.0.tgz",
      "integrity": "sha512-l3FAAOyKJXH2ea6KNFN+MMgC/rnE94YGLXs2ehYqDcCoHt1DpvgWX75BhUJxN38XojP7Ul+4H8PRn7EdyqSDrw==",
      "cpu": [
        "arm64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "freebsd"
      ]
    },
    "node_modules/@rollup/rollup-freebsd-x64": {
      "version": "4.61.0",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-freebsd-x64/-/rollup-freebsd-x64-4.61.0.tgz",
      "integrity": "sha512-VokPN3TSctKj65cyCNPaUh4vMFA8awxOot/0sp+4J7ZlNRKQEhXhawqPwajoi8H5ZFt61i0ugZJuTKXBjGJ17Q==",
      "cpu": [
        "x64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "freebsd"
      ]
    },
    "node_modules/@rollup/rollup-linux-arm-gnueabihf": {
      "version": "4.61.0",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-linux-arm-gnueabihf/-/rollup-linux-arm-gnueabihf-4.61.0.tgz",
      "integrity": "sha512-DxH0P3wxm+Yzs/p3zrk9dw1rURu8p0Nv5+MRK/L7OtnLNg5rLZraSBFZ8iUXOd9f2BlhJyEpIZUH/emjq4UJ4g==",
      "cpu": [
        "arm"
      ],
      "dev": true,
      "libc": [
        "glibc"
      ],
      "license": "MIT",
      "optional": true,
      "os": [
        "linux"
      ]
    },
    "node_modules/@rollup/rollup-linux-arm-musleabihf": {
      "version": "4.61.0",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-linux-arm-musleabihf/-/rollup-linux-arm-musleabihf-4.61.0.tgz",
      "integrity": "sha512-T6ZvMNe84kAz6TBWHC7hGAoEtzP1LWYw/AqayGWEF6uISt3Abk/st06LqRD9THd7Xz3NxzurUpzAuEAUbZf+nw==",
      "cpu": [
        "arm"
      ],
      "dev": true,
      "libc": [
        "musl"
      ],
      "license": "MIT",
      "optional": true,
      "os": [
        "linux"
      ]
    },
    "node_modules/@rollup/rollup-linux-arm64-gnu": {
      "version": "4.61.0",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-linux-arm64-gnu/-/rollup-linux-arm64-gnu-4.61.0.tgz",
      "integrity": "sha512-q/4hzvQkDs8b4jIBab1pnLiiM0ayTZsN2amBFPDzuyZxjEd4wDwx0UJFYM3cOZzSf5Kw8fnWSprJzIBMkcR44Q==",
      "cpu": [
        "arm64"
      ],
      "dev": true,
      "libc": [
        "glibc"
      ],
      "license": "MIT",
      "optional": true,
      "os": [
        "linux"
      ]
    },
    "node_modules/@rollup/rollup-linux-arm64-musl": {
      "version": "4.61.0",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-linux-arm64-musl/-/rollup-linux-arm64-musl-4.61.0.tgz",
      "integrity": "sha512-vvYWX3akdEAY6km+9wAqFDnk6pQsbJKVnj7xawcvs/+fdlYBGp+U+Qq/lLfpIxYIZvZLHMAKD9HLdacSx/r3dw==",
      "cpu": [
        "arm64"
      ],
      "dev": true,
      "libc": [
        "musl"
      ],
      "license": "MIT",
      "optional": true,
      "os": [
        "linux"
      ]
    },
    "node_modules/@rollup/rollup-linux-loong64-gnu": {
      "version": "4.61.0",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-linux-loong64-gnu/-/rollup-linux-loong64-gnu-4.61.0.tgz",
      "integrity": "sha512-DePa5cqOxDP/Zp0VOXpeWaGew5iIv5DXp9NYbzkX5PFQyWVX9184WCTh3hvr/7lhXo8ZVlbFLkz8+o/q1dU6gA==",
      "cpu": [
        "loong64"
      ],
      "dev": true,
      "libc": [
        "glibc"
      ],
      "license": "MIT",
      "optional": true,
      "os": [
        "linux"
      ]
    },
    "node_modules/@rollup/rollup-linux-loong64-musl": {
      "version": "4.61.0",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-linux-loong64-musl/-/rollup-linux-loong64-musl-4.61.0.tgz",
      "integrity": "sha512-LV8aWMB8UChglMCEzs7RkN0GsH29RJaLLqwm9fCIjlqwxQTiWAqNcc7wjBkH31hV0PU/yVxGYvrYsgfea2qw6g==",
      "cpu": [
        "loong64"
      ],
      "dev": true,
      "libc": [
        "musl"
      ],
      "license": "MIT",
      "optional": true,
      "os": [
        "linux"
      ]
    },
    "node_modules/@rollup/rollup-linux-ppc64-gnu": {
      "version": "4.61.0",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-linux-ppc64-gnu/-/rollup-linux-ppc64-gnu-4.61.0.tgz",
      "integrity": "sha512-QoNSnwQtaeNu5grdBbsL0tt1uyl5EnS8DA8Mr3nluMXbhdQNyhN+G4tBax7VCdxLKj8YJ0/4OO9Ho84jMnJtKA==",
      "cpu": [
        "ppc64"
      ],
      "dev": true,
      "libc": [
        "glibc"
      ],
      "license": "MIT",
      "optional": true,
      "os": [
        "linux"
      ]
    },
    "node_modules/@rollup/rollup-linux-ppc64-musl": {
      "version": "4.61.0",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-linux-ppc64-musl/-/rollup-linux-ppc64-musl-4.61.0.tgz",
      "integrity": "sha512-/zZp5MKapIIApE8trN8qLGNSiRN9TUoaUZ1cmVu4XnVdd5LQLOXTtyi+vtfUbNnT3iyjzpPqYeKXmvJ+gJGYWw==",
      "cpu": [
        "ppc64"
      ],
      "dev": true,
      "libc": [
        "musl"
      ],
      "license": "MIT",
      "optional": true,
      "os": [
        "linux"
      ]
    },
    "node_modules/@rollup/rollup-linux-riscv64-gnu": {
      "version": "4.61.0",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-linux-riscv64-gnu/-/rollup-linux-riscv64-gnu-4.61.0.tgz",
      "integrity": "sha512-RbrzcD3aJ1k3UbtMRRBNwojdVVyXjuVAFTfn/xPa6EEl6GE9Sm/akPgFTb9aAC9pMKGJ6CtWxaGrqWcabH+ySg==",
      "cpu": [
        "riscv64"
      ],
      "dev": true,
      "libc": [
        "glibc"
      ],
      "license": "MIT",
      "optional": true,
      "os": [
        "linux"
      ]
    },
    "node_modules/@rollup/rollup-linux-riscv64-musl": {
      "version": "4.61.0",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-linux-riscv64-musl/-/rollup-linux-riscv64-musl-4.61.0.tgz",
      "integrity": "sha512-ZF+onDsBso8PJf1XaG9lB+O9RnBpKGnY6OrzC4CSHrtC1jb6jWLTKK4bRqdoCXHd22gyr2hiYmEAm8Wns/BOCw==",
      "cpu": [
        "riscv64"
      ],
      "dev": true,
      "libc": [
        "musl"
      ],
      "license": "MIT",
      "optional": true,
      "os": [
        "linux"
      ]
    },
    "node_modules/@rollup/rollup-linux-s390x-gnu": {
      "version": "4.61.0",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-linux-s390x-gnu/-/rollup-linux-s390x-gnu-4.61.0.tgz",
      "integrity": "sha512-Atk0aSIk5Zx2Wuh9dgRQgLP0Koc8hOeYpbWryMXyk8G8/HmPkwPPkMqIIDhrXHHYqfUzSJA/I7IWSBv8xSmRBA==",
      "cpu": [
        "s390x"
      ],
      "dev": true,
      "libc": [
        "glibc"
      ],
      "license": "MIT",
      "optional": true,
      "os": [
        "linux"
      ]
    },
    "node_modules/@rollup/rollup-linux-x64-gnu": {
      "version": "4.61.0",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-linux-x64-gnu/-/rollup-linux-x64-gnu-4.61.0.tgz",
      "integrity": "sha512-0uMOcf3eZ5K+K4cYHkdxShFMPlPXCOdfDFEFn9dNYAEEd2cVvmOfH7zFgRVoDgmtQ1m9k5q7qfrHzyMAubKYUA==",
      "cpu": [
        "x64"
      ],
      "dev": true,
      "libc": [
        "glibc"
      ],
      "license": "MIT",
      "optional": true,
      "os": [
        "linux"
      ]
    },
    "node_modules/@rollup/rollup-linux-x64-musl": {
      "version": "4.61.0",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-linux-x64-musl/-/rollup-linux-x64-musl-4.61.0.tgz",
      "integrity": "sha512-mvFtE4A/t/7hRJ7X8Ozmu8FsIkAUat2nzl12pgU337BRmq87AQUJztwHz2Zv5/tjo9/C95E66CK03SI/ToEDJw==",
      "cpu": [
        "x64"
      ],
      "dev": true,
      "libc": [
        "musl"
      ],
      "license": "MIT",
      "optional": true,
      "os": [
        "linux"
      ]
    },
    "node_modules/@rollup/rollup-openbsd-x64": {
      "version": "4.61.0",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-openbsd-x64/-/rollup-openbsd-x64-4.61.0.tgz",
      "integrity": "sha512-z9b9+aTxvt8n2rNltMPvyaUfB8NJ+CVyOrGK/MdIKHx7B+lXmZpm/XbRsU7Rpf3fRqJ2uS6mBJiJveCtq8LHDg==",
      "cpu": [
        "x64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "openbsd"
      ]
    },
    "node_modules/@rollup/rollup-openharmony-arm64": {
      "version": "4.61.0",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-openharmony-arm64/-/rollup-openharmony-arm64-4.61.0.tgz",
      "integrity": "sha512-jXaXFqKMehsOc+g8R6oo33RRC6w07G9jDBxAE5eAKX7mOcCbZloYIPNhfG9Wl+P9O9IWHFO4OJgPi1Ml2qkt7w==",
      "cpu": [
        "arm64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "openharmony"
      ]
    },
    "node_modules/@rollup/rollup-win32-arm64-msvc": {
      "version": "4.61.0",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-win32-arm64-msvc/-/rollup-win32-arm64-msvc-4.61.0.tgz",
      "integrity": "sha512-OXNWVFocS2IA4+QplhTZZ2a+8hPZR7T8KuozsNmJKK8y7cp83StHvGksfHzPG3wczWTczyWHVQuqeiTUbjiyBg==",
      "cpu": [
        "arm64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "win32"
      ]
    },
    "node_modules/@rollup/rollup-win32-ia32-msvc": {
      "version": "4.61.0",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-win32-ia32-msvc/-/rollup-win32-ia32-msvc-4.61.0.tgz",
      "integrity": "sha512-AlAbNtBO637LxSldqV43z0FfXoGfl2TW1DgAg/bs7aQswFbDewz2SJm3BUhiGfbOVtW571xbc9p+REdxhyN/Eg==",
      "cpu": [
        "ia32"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "win32"
      ]
    },
    "node_modules/@rollup/rollup-win32-x64-gnu": {
      "version": "4.61.0",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-win32-x64-gnu/-/rollup-win32-x64-gnu-4.61.0.tgz",
      "integrity": "sha512-QRSrQXyJ1M4tjNXdR0/G/IgV6lzfQQJYBjlWIEYkY2Xs86DRl/iEpQ4blMDjJxSl7n19eDKKXMg0AmuBVYy8pQ==",
      "cpu": [
        "x64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "win32"
      ]
    },
    "node_modules/@rollup/rollup-win32-x64-msvc": {
      "version": "4.61.0",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-win32-x64-msvc/-/rollup-win32-x64-msvc-4.61.0.tgz",
      "integrity": "sha512-tkuFxhvKO/HlGd0VsINF6vHSYH8AF8W0TcNxKDK6JZmrehngFj78pToc8iemtnvwilDjs2G/qSzYFhe9U8q+fw==",
      "cpu": [
        "x64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "win32"
      ]
    },
    "node_modules/@types/estree": {
      "version": "1.0.9",
      "resolved": "https://registry.npmjs.org/@types/estree/-/estree-1.0.9.tgz",
      "integrity": "sha512-GhdPgy1el4/ImP05X05Uw4cw2/M93BCUmnEvWZNStlCzEKME4Fkk+YpoA5OiHNQmoS7Cafb8Xa3Pya8m1Qrzeg==",
      "dev": true,
      "license": "MIT"
    },
    "node_modules/@vitejs/plugin-vue": {
      "version": "5.2.4",
      "resolved": "https://registry.npmjs.org/@vitejs/plugin-vue/-/plugin-vue-5.2.4.tgz",
      "integrity": "sha512-7Yx/SXSOcQq5HiiV3orevHUFn+pmMB4cgbEkDYgnkUWb0WfeQ/wa2yFv6D5ICiCQOVpjA7vYDXrC7AGO8yjDHA==",
      "dev": true,
      "license": "MIT",
      "engines": {
        "node": "^18.0.0 || >=20.0.0"
      },
      "peerDependencies": {
        "vite": "^5.0.0 || ^6.0.0",
        "vue": "^3.2.25"
      }
    },
    "node_modules/@volar/language-core": {
      "version": "2.4.15",
      "resolved": "https://registry.npmjs.org/@volar/language-core/-/language-core-2.4.15.tgz",
      "integrity": "sha512-3VHw+QZU0ZG9IuQmzT68IyN4hZNd9GchGPhbD9+pa8CVv7rnoOZwo7T8weIbrRmihqy3ATpdfXFnqRrfPVK6CA==",
      "dev": true,
      "license": "MIT",
      "dependencies": {
        "@volar/source-map": "2.4.15"
      }
    },
    "node_modules/@volar/source-map": {
      "version": "2.4.15",
      "resolved": "https://registry.npmjs.org/@volar/source-map/-/source-map-2.4.15.tgz",
      "integrity": "sha512-CPbMWlUN6hVZJYGcU/GSoHu4EnCHiLaXI9n8c9la6RaI9W5JHX+NqG+GSQcB0JdC2FIBLdZJwGsfKyBB71VlTg==",
      "dev": true,
      "license": "MIT"
    },
    "node_modules/@volar/typescript": {
      "version": "2.4.15",
      "resolved": "https://registry.npmjs.org/@volar/typescript/-/typescript-2.4.15.tgz",
      "integrity": "sha512-2aZ8i0cqPGjXb4BhkMsPYDkkuc2ZQ6yOpqwAuNwUoncELqoy5fRgOQtLR9gB0g902iS0NAkvpIzs27geVyVdPg==",
      "dev": true,
      "license": "MIT",
      "dependencies": {
        "@volar/language-core": "2.4.15",
        "path-browserify": "^1.0.1",
        "vscode-uri": "^3.0.8"
      }
    },
    "node_modules/@vue/compiler-core": {
      "version": "3.5.35",
      "resolved": "https://registry.npmjs.org/@vue/compiler-core/-/compiler-core-3.5.35.tgz",
      "integrity": "sha512-BUmHaR1J+O+CKZ9uJucdVTEr1LHsdyvv7vG3eNRhK3CczEHeMd/LtsHAuD7PbrxvI2envCY2v7HI1vC1aBRzKw==",
      "license": "MIT",
      "dependencies": {
        "@babel/parser": "^7.29.3",
        "@vue/shared": "3.5.35",
        "entities": "^7.0.1",
        "estree-walker": "^2.0.2",
        "source-map-js": "^1.2.1"
      }
    },
    "node_modules/@vue/compiler-dom": {
      "version": "3.5.35",
      "resolved": "https://registry.npmjs.org/@vue/compiler-dom/-/compiler-dom-3.5.35.tgz",
      "integrity": "sha512-k+bprkXxuqhVajgTx5mUHuir7TwQzUKOWR40ng1ncAqQRPnrLngGGgqVEEhOnTMlc8btHYVKmrP8s5Qyg0hvYA==",
      "license": "MIT",
      "dependencies": {
        "@vue/compiler-core": "3.5.35",
        "@vue/shared": "3.5.35"
      }
    },
    "node_modules/@vue/compiler-sfc": {
      "version": "3.5.35",
      "resolved": "https://registry.npmjs.org/@vue/compiler-sfc/-/compiler-sfc-3.5.35.tgz",
      "integrity": "sha512-G5VPMcXTSywXBgtFOZOnHKBxKSrwXUcvY1iaF5/hRcy7t0J6CH/d8ha9F4nzi00Fax1eLV0QHM7v4mQu68jydw==",
      "license": "MIT",
      "dependencies": {
        "@babel/parser": "^7.29.3",
        "@vue/compiler-core": "3.5.35",
        "@vue/compiler-dom": "3.5.35",
        "@vue/compiler-ssr": "3.5.35",
        "@vue/shared": "3.5.35",
        "estree-walker": "^2.0.2",
        "magic-string": "^0.30.21",
        "postcss": "^8.5.15",
        "source-map-js": "^1.2.1"
      }
    },
    "node_modules/@vue/compiler-ssr": {
      "version": "3.5.35",
      "resolved": "https://registry.npmjs.org/@vue/compiler-ssr/-/compiler-ssr-3.5.35.tgz",
      "integrity": "sha512-rGhAeXgdM7/ffTJGXT69rCCdTmjDewnFuUZfBQQHTdcEBeWdT5HCGY60y2ytLJr9/Dsu7IntUi5z/w0h6Rjnzw==",
      "license": "MIT",
      "dependencies": {
        "@vue/compiler-dom": "3.5.35",
        "@vue/shared": "3.5.35"
      }
    },
    "node_modules/@vue/compiler-vue2": {
      "version": "2.7.16",
      "resolved": "https://registry.npmjs.org/@vue/compiler-vue2/-/compiler-vue2-2.7.16.tgz",
      "integrity": "sha512-qYC3Psj9S/mfu9uVi5WvNZIzq+xnXMhOwbTFKKDD7b1lhpnn71jXSFdTQ+WsIEk0ONCd7VV2IMm7ONl6tbQ86A==",
      "dev": true,
      "license": "MIT",
      "dependencies": {
        "de-indent": "^1.0.2",
        "he": "^1.2.0"
      }
    },
    "node_modules/@vue/devtools-api": {
      "version": "6.6.4",
      "resolved": "https://registry.npmjs.org/@vue/devtools-api/-/devtools-api-6.6.4.tgz",
      "integrity": "sha512-sGhTPMuXqZ1rVOk32RylztWkfXTRhuS7vgAKv0zjqk8gbsHkJ7xfFf+jbySxt7tWObEJwyKaHMikV/WGDiQm8g==",
      "license": "MIT"
    },
    "node_modules/@vue/language-core": {
      "version": "2.2.12",
      "resolved": "https://registry.npmjs.org/@vue/language-core/-/language-core-2.2.12.tgz",
      "integrity": "sha512-IsGljWbKGU1MZpBPN+BvPAdr55YPkj2nB/TBNGNC32Vy2qLG25DYu/NBN2vNtZqdRbTRjaoYrahLrToim2NanA==",
      "dev": true,
      "license": "MIT",
      "dependencies": {
        "@volar/language-core": "2.4.15",
        "@vue/compiler-dom": "^3.5.0",
        "@vue/compiler-vue2": "^2.7.16",
        "@vue/shared": "^3.5.0",
        "alien-signals": "^1.0.3",
        "minimatch": "^9.0.3",
        "muggle-string": "^0.4.1",
        "path-browserify": "^1.0.1"
      },
      "peerDependencies": {
        "typescript": "*"
      },
      "peerDependenciesMeta": {
        "typescript": {
          "optional": true
        }
      }
    },
    "node_modules/@vue/reactivity": {
      "version": "3.5.35",
      "resolved": "https://registry.npmjs.org/@vue/reactivity/-/reactivity-3.5.35.tgz",
      "integrity": "sha512-tVc+SsHConvh/Lz64qq1pP3rYArBmK42xonovEcxY74SQtvctZodG/zhq54P5dr38cVuw25d27cPNRdlMidpGQ==",
      "license": "MIT",
      "dependencies": {
        "@vue/shared": "3.5.35"
      }
    },
    "node_modules/@vue/runtime-core": {
      "version": "3.5.35",
      "resolved": "https://registry.npmjs.org/@vue/runtime-core/-/runtime-core-3.5.35.tgz",
      "integrity": "sha512-A/xFNX9loIcWDygeQuNCfKuh0CoYBzxhqEMNah5TSFg9Z53DrFYEN2qi5CU9necjM1OWYegYREUTHmXTmhfXtg==",
      "license": "MIT",
      "dependencies": {
        "@vue/reactivity": "3.5.35",
        "@vue/shared": "3.5.35"
      }
    },
    "node_modules/@vue/runtime-dom": {
      "version": "3.5.35",
      "resolved": "https://registry.npmjs.org/@vue/runtime-dom/-/runtime-dom-3.5.35.tgz",
      "integrity": "sha512-odrJ1C391dbGnyDRh8U+rnP7J2amIEzfmRk5vXy7xi3aZhEXofTvpi0T4HJb6jlNqQZTNPR5MPHSB3RHNkIORA==",
      "license": "MIT",
      "dependencies": {
        "@vue/reactivity": "3.5.35",
        "@vue/runtime-core": "3.5.35",
        "@vue/shared": "3.5.35",
        "csstype": "^3.2.3"
      }
    },
    "node_modules/@vue/server-renderer": {
      "version": "3.5.35",
      "resolved": "https://registry.npmjs.org/@vue/server-renderer/-/server-renderer-3.5.35.tgz",
      "integrity": "sha512-NkebSOYdB97wi8OQcO3HqzZSlymJi/aWsN/7h74OSVhRTm6qGs3Jp3e0rCXynmWwSlKeRrnlIug+ilYoHBmQDA==",
      "license": "MIT",
      "dependencies": {
        "@vue/compiler-ssr": "3.5.35",
        "@vue/shared": "3.5.35"
      },
      "peerDependencies": {
        "vue": "3.5.35"
      }
    },
    "node_modules/@vue/shared": {
      "version": "3.5.35",
      "resolved": "https://registry.npmjs.org/@vue/shared/-/shared-3.5.35.tgz",
      "integrity": "sha512-zSbjL7gRXwks2ZQLRGCajBtBXEOXW9Ddhn/HvSdrGkE2dqGnumzW8XtusRrxrE9LvqtiqDXQ+A60Hp6mvdYxfA==",
      "license": "MIT"
    },
    "node_modules/alien-signals": {
      "version": "1.0.13",
      "resolved": "https://registry.npmjs.org/alien-signals/-/alien-signals-1.0.13.tgz",
      "integrity": "sha512-OGj9yyTnJEttvzhTUWuscOvtqxq5vrhF7vL9oS0xJ2mK0ItPYP1/y+vCFebfxoEyAz0++1AIwJ5CMr+Fk3nDmg==",
      "dev": true,
      "license": "MIT"
    },
    "node_modules/balanced-match": {
      "version": "1.0.2",
      "resolved": "https://registry.npmjs.org/balanced-match/-/balanced-match-1.0.2.tgz",
      "integrity": "sha512-3oSeUO0TMV67hN1AmbXsK4yaqU7tjiHlbxRDZOpH0KW9+CeX4bRAaX0Anxt0tx2MrpRpWwQaPwIlISEJhYU5Pw==",
      "dev": true,
      "license": "MIT"
    },
    "node_modules/brace-expansion": {
      "version": "2.1.1",
      "resolved": "https://registry.npmjs.org/brace-expansion/-/brace-expansion-2.1.1.tgz",
      "integrity": "sha512-WR1cURNjuvBLMZBMbqM0UoE+WAfdUcEV1ccD8PVBVOI+Z3ND4+SZbN8RsfT2bMuG1qwz5RFvPukSZm5fF2D5eA==",
      "dev": true,
      "license": "MIT",
      "dependencies": {
        "balanced-match": "^1.0.0"
      }
    },
    "node_modules/csstype": {
      "version": "3.2.3",
      "resolved": "https://registry.npmjs.org/csstype/-/csstype-3.2.3.tgz",
      "integrity": "sha512-z1HGKcYy2xA8AGQfwrn0PAy+PB7X/GSj3UVJW9qKyn43xWa+gl5nXmU4qqLMRzWVLFC8KusUX8T/0kCiOYpAIQ==",
      "license": "MIT"
    },
    "node_modules/de-indent": {
      "version": "1.0.2",
      "resolved": "https://registry.npmjs.org/de-indent/-/de-indent-1.0.2.tgz",
      "integrity": "sha512-e/1zu3xH5MQryN2zdVaF0OrdNLUbvWxzMbi+iNA6Bky7l1RoP8a2fIbRocyHclXt/arDrrR6lL3TqFD9pMQTsg==",
      "dev": true,
      "license": "MIT"
    },
    "node_modules/entities": {
      "version": "7.0.1",
      "resolved": "https://registry.npmjs.org/entities/-/entities-7.0.1.tgz",
      "integrity": "sha512-TWrgLOFUQTH994YUyl1yT4uyavY5nNB5muff+RtWaqNVCAK408b5ZnnbNAUEWLTCpum9w6arT70i1XdQ4UeOPA==",
      "license": "BSD-2-Clause",
      "engines": {
        "node": ">=0.12"
      },
      "funding": {
        "url": "https://github.com/fb55/entities?sponsor=1"
      }
    },
    "node_modules/esbuild": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/esbuild/-/esbuild-0.25.12.tgz",
      "integrity": "sha512-bbPBYYrtZbkt6Os6FiTLCTFxvq4tt3JKall1vRwshA3fdVztsLAatFaZobhkBC8/BrPetoa0oksYoKXoG4ryJg==",
      "dev": true,
      "hasInstallScript": true,
      "license": "MIT",
      "bin": {
        "esbuild": "bin/esbuild"
      },
      "engines": {
        "node": ">=18"
      },
      "optionalDependencies": {
        "@esbuild/aix-ppc64": "0.25.12",
        "@esbuild/android-arm": "0.25.12",
        "@esbuild/android-arm64": "0.25.12",
        "@esbuild/android-x64": "0.25.12",
        "@esbuild/darwin-arm64": "0.25.12",
        "@esbuild/darwin-x64": "0.25.12",
        "@esbuild/freebsd-arm64": "0.25.12",
        "@esbuild/freebsd-x64": "0.25.12",
        "@esbuild/linux-arm": "0.25.12",
        "@esbuild/linux-arm64": "0.25.12",
        "@esbuild/linux-ia32": "0.25.12",
        "@esbuild/linux-loong64": "0.25.12",
        "@esbuild/linux-mips64el": "0.25.12",
        "@esbuild/linux-ppc64": "0.25.12",
        "@esbuild/linux-riscv64": "0.25.12",
        "@esbuild/linux-s390x": "0.25.12",
        "@esbuild/linux-x64": "0.25.12",
        "@esbuild/netbsd-arm64": "0.25.12",
        "@esbuild/netbsd-x64": "0.25.12",
        "@esbuild/openbsd-arm64": "0.25.12",
        "@esbuild/openbsd-x64": "0.25.12",
        "@esbuild/openharmony-arm64": "0.25.12",
        "@esbuild/sunos-x64": "0.25.12",
        "@esbuild/win32-arm64": "0.25.12",
        "@esbuild/win32-ia32": "0.25.12",
        "@esbuild/win32-x64": "0.25.12"
      }
    },
    "node_modules/estree-walker": {
      "version": "2.0.2",
      "resolved": "https://registry.npmjs.org/estree-walker/-/estree-walker-2.0.2.tgz",
      "integrity": "sha512-Rfkk/Mp/DL7JVje3u18FxFujQlTNR2q6QfMSMB7AvCBx91NGj/ba3kCfza0f6dVDbw7YlRf/nDrn7pQrCCyQ/w==",
      "license": "MIT"
    },
    "node_modules/fdir": {
      "version": "6.5.0",
      "resolved": "https://registry.npmjs.org/fdir/-/fdir-6.5.0.tgz",
      "integrity": "sha512-tIbYtZbucOs0BRGqPJkshJUYdL+SDH7dVM8gjy+ERp3WAUjLEFJE+02kanyHtwjWOnwrKYBiwAmM0p4kLJAnXg==",
      "dev": true,
      "license": "MIT",
      "engines": {
        "node": ">=12.0.0"
      },
      "peerDependencies": {
        "picomatch": "^3 || ^4"
      },
      "peerDependenciesMeta": {
        "picomatch": {
          "optional": true
        }
      }
    },
    "node_modules/fsevents": {
      "version": "2.3.3",
      "resolved": "https://registry.npmjs.org/fsevents/-/fsevents-2.3.3.tgz",
      "integrity": "sha512-5xoDfX+fL7faATnagmWPpbFtwh/R77WmMMqqHGS65C3vvB0YHrgF+B1YmZ3441tMj5n63k0212XNoJwzlhffQw==",
      "dev": true,
      "hasInstallScript": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "darwin"
      ],
      "engines": {
        "node": "^8.16.0 || ^10.6.0 || >=11.0.0"
      }
    },
    "node_modules/he": {
      "version": "1.2.0",
      "resolved": "https://registry.npmjs.org/he/-/he-1.2.0.tgz",
      "integrity": "sha512-F/1DnUGPopORZi0ni+CvrCgHQ5FyEAHRLSApuYWMmrbSwoN2Mn/7k+Gl38gJnR7yyDZk6WLXwiGod1JOWNDKGw==",
      "dev": true,
      "license": "MIT",
      "bin": {
        "he": "bin/he"
      }
    },
    "node_modules/magic-string": {
      "version": "0.30.21",
      "resolved": "https://registry.npmjs.org/magic-string/-/magic-string-0.30.21.tgz",
      "integrity": "sha512-vd2F4YUyEXKGcLHoq+TEyCjxueSeHnFxyyjNp80yg0XV4vUhnDer/lvvlqM/arB5bXQN5K2/3oinyCRyx8T2CQ==",
      "license": "MIT",
      "dependencies": {
        "@jridgewell/sourcemap-codec": "^1.5.5"
      }
    },
    "node_modules/minimatch": {
      "version": "9.0.9",
      "resolved": "https://registry.npmjs.org/minimatch/-/minimatch-9.0.9.tgz",
      "integrity": "sha512-OBwBN9AL4dqmETlpS2zasx+vTeWclWzkblfZk7KTA5j3jeOONz/tRCnZomUyvNg83wL5Zv9Ss6HMJXAgL8R2Yg==",
      "dev": true,
      "license": "ISC",
      "dependencies": {
        "brace-expansion": "^2.0.2"
      },
      "engines": {
        "node": ">=16 || 14 >=14.17"
      },
      "funding": {
        "url": "https://github.com/sponsors/isaacs"
      }
    },
    "node_modules/muggle-string": {
      "version": "0.4.1",
      "resolved": "https://registry.npmjs.org/muggle-string/-/muggle-string-0.4.1.tgz",
      "integrity": "sha512-VNTrAak/KhO2i8dqqnqnAHOa3cYBwXEZe9h+D5h/1ZqFSTEFHdM65lR7RoIqq3tBBYavsOXV84NoHXZ0AkPyqQ==",
      "dev": true,
      "license": "MIT"
    },
    "node_modules/nanoid": {
      "version": "3.3.12",
      "resolved": "https://registry.npmjs.org/nanoid/-/nanoid-3.3.12.tgz",
      "integrity": "sha512-ZB9RH/39qpq5Vu6Y+NmUaFhQR6pp+M2Xt76XBnEwDaGcVAqhlvxrl3B2bKS5D3NH3QR76v3aSrKaF/Kiy7lEtQ==",
      "funding": [
        {
          "type": "github",
          "url": "https://github.com/sponsors/ai"
        }
      ],
      "license": "MIT",
      "bin": {
        "nanoid": "bin/nanoid.cjs"
      },
      "engines": {
        "node": "^10 || ^12 || ^13.7 || ^14 || >=15.0.1"
      }
    },
    "node_modules/path-browserify": {
      "version": "1.0.1",
      "resolved": "https://registry.npmjs.org/path-browserify/-/path-browserify-1.0.1.tgz",
      "integrity": "sha512-b7uo2UCUOYZcnF/3ID0lulOJi/bafxa1xPe7ZPsammBSpjSWQkjNxlt635YGS2MiR9GjvuXCtz2emr3jbsz98g==",
      "dev": true,
      "license": "MIT"
    },
    "node_modules/picocolors": {
      "version": "1.1.1",
      "resolved": "https://registry.npmjs.org/picocolors/-/picocolors-1.1.1.tgz",
      "integrity": "sha512-xceH2snhtb5M9liqDsmEw56le376mTZkEX/jEb/RxNFyegNul7eNslCXP9FDj/Lcu0X8KEyMceP2ntpaHrDEVA==",
      "license": "ISC"
    },
    "node_modules/picomatch": {
      "version": "4.0.4",
      "resolved": "https://registry.npmjs.org/picomatch/-/picomatch-4.0.4.tgz",
      "integrity": "sha512-QP88BAKvMam/3NxH6vj2o21R6MjxZUAd6nlwAS/pnGvN9IVLocLHxGYIzFhg6fUQ+5th6P4dv4eW9jX3DSIj7A==",
      "dev": true,
      "license": "MIT",
      "engines": {
        "node": ">=12"
      },
      "funding": {
        "url": "https://github.com/sponsors/jonschlinkert"
      }
    },
    "node_modules/postcss": {
      "version": "8.5.15",
      "resolved": "https://registry.npmjs.org/postcss/-/postcss-8.5.15.tgz",
      "integrity": "sha512-FfR8sjd4em2T6fb3I2MwAJU7HWVMr9zba+enmQeeWFfCbm+UOC/0X4DS8XtpUTMwWMGbjKYP7xjfNekzyGmB3A==",
      "funding": [
        {
          "type": "opencollective",
          "url": "https://opencollective.com/postcss/"
        },
        {
          "type": "tidelift",
          "url": "https://tidelift.com/funding/github/npm/postcss"
        },
        {
          "type": "github",
          "url": "https://github.com/sponsors/ai"
        }
      ],
      "license": "MIT",
      "dependencies": {
        "nanoid": "^3.3.12",
        "picocolors": "^1.1.1",
        "source-map-js": "^1.2.1"
      },
      "engines": {
        "node": "^10 || ^12 || >=14"
      }
    },
    "node_modules/primeicons": {
      "version": "7.0.0",
      "resolved": "https://registry.npmjs.org/primeicons/-/primeicons-7.0.0.tgz",
      "integrity": "sha512-jK3Et9UzwzTsd6tzl2RmwrVY/b8raJ3QZLzoDACj+oTJ0oX7L9Hy+XnVwgo4QVKlKpnP/Ur13SXV/pVh4LzaDw==",
      "license": "MIT"
    },
    "node_modules/primevue": {
      "version": "4.5.5",
      "resolved": "https://registry.npmjs.org/primevue/-/primevue-4.5.5.tgz",
      "integrity": "sha512-Kv5REIewCdP806QaoU+4nBXfmpzOGFKkZ9qH4KsL6MjiAQVc4PUzypt8erl4r3Vzh3nr3aWZIxkxYRRsLGiX2A==",
      "license": "MIT",
      "dependencies": {
        "@primeuix/styled": "^0.7.4",
        "@primeuix/styles": "^2.0.3",
        "@primeuix/utils": "^0.6.2",
        "@primevue/core": "4.5.5",
        "@primevue/icons": "4.5.5"
      },
      "engines": {
        "node": ">=12.11.0"
      }
    },
    "node_modules/rollup": {
      "version": "4.61.0",
      "resolved": "https://registry.npmjs.org/rollup/-/rollup-4.61.0.tgz",
      "integrity": "sha512-T9mWdbWfQtp0B5lv/HX+wrhYsmXRlcWnXXmJbXqKJhlRaoS6KMhq0gpyzW4UJfclcxrEdLnTgjT2NjruLONu0g==",
      "dev": true,
      "license": "MIT",
      "dependencies": {
        "@types/estree": "1.0.9"
      },
      "bin": {
        "rollup": "dist/bin/rollup"
      },
      "engines": {
        "node": ">=18.0.0",
        "npm": ">=8.0.0"
      },
      "optionalDependencies": {
        "@rollup/rollup-android-arm-eabi": "4.61.0",
        "@rollup/rollup-android-arm64": "4.61.0",
        "@rollup/rollup-darwin-arm64": "4.61.0",
        "@rollup/rollup-darwin-x64": "4.61.0",
        "@rollup/rollup-freebsd-arm64": "4.61.0",
        "@rollup/rollup-freebsd-x64": "4.61.0",
        "@rollup/rollup-linux-arm-gnueabihf": "4.61.0",
        "@rollup/rollup-linux-arm-musleabihf": "4.61.0",
        "@rollup/rollup-linux-arm64-gnu": "4.61.0",
        "@rollup/rollup-linux-arm64-musl": "4.61.0",
        "@rollup/rollup-linux-loong64-gnu": "4.61.0",
        "@rollup/rollup-linux-loong64-musl": "4.61.0",
        "@rollup/rollup-linux-ppc64-gnu": "4.61.0",
        "@rollup/rollup-linux-ppc64-musl": "4.61.0",
        "@rollup/rollup-linux-riscv64-gnu": "4.61.0",
        "@rollup/rollup-linux-riscv64-musl": "4.61.0",
        "@rollup/rollup-linux-s390x-gnu": "4.61.0",
        "@rollup/rollup-linux-x64-gnu": "4.61.0",
        "@rollup/rollup-linux-x64-musl": "4.61.0",
        "@rollup/rollup-openbsd-x64": "4.61.0",
        "@rollup/rollup-openharmony-arm64": "4.61.0",
        "@rollup/rollup-win32-arm64-msvc": "4.61.0",
        "@rollup/rollup-win32-ia32-msvc": "4.61.0",
        "@rollup/rollup-win32-x64-gnu": "4.61.0",
        "@rollup/rollup-win32-x64-msvc": "4.61.0",
        "fsevents": "~2.3.2"
      }
    },
    "node_modules/source-map-js": {
      "version": "1.2.1",
      "resolved": "https://registry.npmjs.org/source-map-js/-/source-map-js-1.2.1.tgz",
      "integrity": "sha512-UXWMKhLOwVKb728IUtQPXxfYU+usdybtUrK/8uGE8CQMvrhOpwvzDBwj0QhSL7MQc7vIsISBG8VQ8+IDQxpfQA==",
      "license": "BSD-3-Clause",
      "engines": {
        "node": ">=0.10.0"
      }
    },
    "node_modules/tinyglobby": {
      "version": "0.2.17",
      "resolved": "https://registry.npmjs.org/tinyglobby/-/tinyglobby-0.2.17.tgz",
      "integrity": "sha512-wXR/dYpcqKmfWpEdZjiKJOwCNFndD0DMnrW/cYjVGttEkBfVgcLFHoNrlj47mjOVic9yyNu65alsgF4NQyTa2g==",
      "dev": true,
      "license": "MIT",
      "dependencies": {
        "fdir": "^6.5.0",
        "picomatch": "^4.0.4"
      },
      "engines": {
        "node": ">=12.0.0"
      },
      "funding": {
        "url": "https://github.com/sponsors/SuperchupuDev"
      }
    },
    "node_modules/typescript": {
      "version": "5.8.3",
      "resolved": "https://registry.npmjs.org/typescript/-/typescript-5.8.3.tgz",
      "integrity": "sha512-p1diW6TqL9L07nNxvRMM7hMMw4c5XOo/1ibL4aAIGmSAt9slTE1Xgw5KWuof2uTOvCg9BY7ZRi+GaF+7sfgPeQ==",
      "devOptional": true,
      "license": "Apache-2.0",
      "bin": {
        "tsc": "bin/tsc",
        "tsserver": "bin/tsserver"
      },
      "engines": {
        "node": ">=14.17"
      }
    },
    "node_modules/vite": {
      "version": "6.4.3",
      "resolved": "https://registry.npmjs.org/vite/-/vite-6.4.3.tgz",
      "integrity": "sha512-NTKlcQjlAK7MlQoyb6LgaqHc8sso/pVyUJYWMws3jg21uTJw/LddqIFPcPqP6PzpgbIcZyKI85sFE4HBrQDA8A==",
      "dev": true,
      "license": "MIT",
      "dependencies": {
        "esbuild": "^0.25.0",
        "fdir": "^6.4.4",
        "picomatch": "^4.0.2",
        "postcss": "^8.5.3",
        "rollup": "^4.34.9",
        "tinyglobby": "^0.2.13"
      },
      "bin": {
        "vite": "bin/vite.js"
      },
      "engines": {
        "node": "^18.0.0 || ^20.0.0 || >=22.0.0"
      },
      "funding": {
        "url": "https://github.com/vitejs/vite?sponsor=1"
      },
      "optionalDependencies": {
        "fsevents": "~2.3.3"
      },
      "peerDependencies": {
        "@types/node": "^18.0.0 || ^20.0.0 || >=22.0.0",
        "jiti": ">=1.21.0",
        "less": "*",
        "lightningcss": "^1.21.0",
        "sass": "*",
        "sass-embedded": "*",
        "stylus": "*",
        "sugarss": "*",
        "terser": "^5.16.0",
        "tsx": "^4.8.1",
        "yaml": "^2.4.2"
      },
      "peerDependenciesMeta": {
        "@types/node": {
          "optional": true
        },
        "jiti": {
          "optional": true
        },
        "less": {
          "optional": true
        },
        "lightningcss": {
          "optional": true
        },
        "sass": {
          "optional": true
        },
        "sass-embedded": {
          "optional": true
        },
        "stylus": {
          "optional": true
        },
        "sugarss": {
          "optional": true
        },
        "terser": {
          "optional": true
        },
        "tsx": {
          "optional": true
        },
        "yaml": {
          "optional": true
        }
      }
    },
    "node_modules/vscode-uri": {
      "version": "3.1.0",
      "resolved": "https://registry.npmjs.org/vscode-uri/-/vscode-uri-3.1.0.tgz",
      "integrity": "sha512-/BpdSx+yCQGnCvecbyXdxHDkuk55/G3xwnC0GqY4gmQ3j+A+g8kzzgB4Nk/SINjqn6+waqw3EgbVF2QKExkRxQ==",
      "dev": true,
      "license": "MIT"
    },
    "node_modules/vue": {
      "version": "3.5.35",
      "resolved": "https://registry.npmjs.org/vue/-/vue-3.5.35.tgz",
      "integrity": "sha512-cx89fnr+0kVGHiNFG6y6s0bdjypJRFNZn6x3WPstNdQR1bi1mbB7h4v5IBGTsPJU3nK1+0Iqj3Zf+hZWMieR4Q==",
      "license": "MIT",
      "dependencies": {
        "@vue/compiler-dom": "3.5.35",
        "@vue/compiler-sfc": "3.5.35",
        "@vue/runtime-dom": "3.5.35",
        "@vue/server-renderer": "3.5.35",
        "@vue/shared": "3.5.35"
      },
      "peerDependencies": {
        "typescript": "*"
      },
      "peerDependenciesMeta": {
        "typescript": {
          "optional": true
        }
      }
    },
    "node_modules/vue-i18n": {
      "version": "10.0.8",
      "resolved": "https://registry.npmjs.org/vue-i18n/-/vue-i18n-10.0.8.tgz",
      "integrity": "sha512-mIjy4utxMz9lMMo6G9vYePv7gUFt4ztOMhY9/4czDJxZ26xPeJ49MAGa9wBAE3XuXbYCrtVPmPxNjej7JJJkZQ==",
      "deprecated": "v9 and v10 no longer supported. please migrate to v11. about maintenance status, see https://vue-i18n.intlify.dev/guide/maintenance.html",
      "license": "MIT",
      "dependencies": {
        "@intlify/core-base": "10.0.8",
        "@intlify/shared": "10.0.8",
        "@vue/devtools-api": "^6.5.0"
      },
      "engines": {
        "node": ">= 16"
      },
      "funding": {
        "url": "https://github.com/sponsors/kazupon"
      },
      "peerDependencies": {
        "vue": "^3.0.0"
      }
    },
    "node_modules/vue-router": {
      "version": "4.6.4",
      "resolved": "https://registry.npmjs.org/vue-router/-/vue-router-4.6.4.tgz",
      "integrity": "sha512-Hz9q5sa33Yhduglwz6g9skT8OBPii+4bFn88w6J+J4MfEo4KRRpmiNG/hHHkdbRFlLBOqxN8y8gf2Fb0MTUgVg==",
      "license": "MIT",
      "dependencies": {
        "@vue/devtools-api": "^6.6.4"
      },
      "funding": {
        "url": "https://github.com/sponsors/posva"
      },
      "peerDependencies": {
        "vue": "^3.5.0"
      }
    },
    "node_modules/vue-tsc": {
      "version": "2.2.12",
      "resolved": "https://registry.npmjs.org/vue-tsc/-/vue-tsc-2.2.12.tgz",
      "integrity": "sha512-P7OP77b2h/Pmk+lZdJ0YWs+5tJ6J2+uOQPo7tlBnY44QqQSPYvS0qVT4wqDJgwrZaLe47etJLLQRFia71GYITw==",
      "dev": true,
      "license": "MIT",
      "dependencies": {
        "@volar/typescript": "2.4.15",
        "@vue/language-core": "2.2.12"
      },
      "bin": {
        "vue-tsc": "bin/vue-tsc.js"
      },
      "peerDependencies": {
        "typescript": ">=5.0.0"
      }
    },
    "node_modules/zod": {
      "version": "3.25.76",
      "resolved": "https://registry.npmjs.org/zod/-/zod-3.25.76.tgz",
      "integrity": "sha512-gzUt/qt81nXsFGKIFcC3YnfEAx5NkunCfnDlvuBSSFS02bcXu4Lmea0AFIUwbLWxWPx3d9p8S5QoaujKcNQxcQ==",
      "license": "MIT",
      "funding": {
        "url": "https://github.com/sponsors/colinhacks"
      }
    }
  }
}

```

`package-lock.json`:

```json
{
  "name": "resumainer-frontend",
  "version": "0.1.0",
  "lockfileVersion": 3,
  "requires": true,
  "packages": {
    "": {
      "name": "resumainer-frontend",
      "version": "0.1.0",
      "dependencies": {
        "@primeuix/themes": "^1.0.2",
        "@primevue/forms": "^4.3.3",
        "primeicons": "^7.0.0",
        "primevue": "^4.3.3",
        "vue": "^3.5.13",
        "vue-i18n": "^10.0.7",
        "vue-router": "^4.5.0",
        "zod": "^3.24.3"
      },
      "devDependencies": {
        "@vitejs/plugin-vue": "^5.2.3",
        "typescript": "~5.8.3",
        "vite": "^6.3.5",
        "vue-tsc": "^2.2.8"
      }
    },
    "node_modules/@babel/helper-string-parser": {
      "version": "7.29.7",
      "resolved": "https://registry.npmjs.org/@babel/helper-string-parser/-/helper-string-parser-7.29.7.tgz",
      "integrity": "sha512-Pb5ijPrZ89GDH8223L4UP8i6QApWxs04RbPQJTeWDV0/keR2E36MeKnyr6LYmUUvqRRI+Iv87SuF1W6ErINzYw==",
      "license": "MIT",
      "engines": {
        "node": ">=6.9.0"
      }
    },
    "node_modules/@babel/helper-validator-identifier": {
      "version": "7.29.7",
      "resolved": "https://registry.npmjs.org/@babel/helper-validator-identifier/-/helper-validator-identifier-7.29.7.tgz",
      "integrity": "sha512-qehxGkRj55h/ff8EMaJ+cYhyaKlHIxqYDn682wQD7RNp9UujOQsHog2uS0r2vzr4pW+sXf90NeeayjcNaX3fFg==",
      "license": "MIT",
      "engines": {
        "node": ">=6.9.0"
      }
    },
    "node_modules/@babel/parser": {
      "version": "7.29.7",
      "resolved": "https://registry.npmjs.org/@babel/parser/-/parser-7.29.7.tgz",
      "integrity": "sha512-hnORnjP/1P/zFEndoeX+n+t1RwWRJiJpM/jO7FW32Kn9r5+sJB2JWOdYo4L6k78j15eCwY3Gm/7364B1EMwtNg==",
      "license": "MIT",
      "dependencies": {
        "@babel/types": "^7.29.7"
      },
      "bin": {
        "parser": "bin/babel-parser.js"
      },
      "engines": {
        "node": ">=6.0.0"
      }
    },
    "node_modules/@babel/types": {
      "version": "7.29.7",
      "resolved": "https://registry.npmjs.org/@babel/types/-/types-7.29.7.tgz",
      "integrity": "sha512-4zBIxpPzowiZpusoFkyGVwakdRJUyuH5PxQ/PrqghfdFWWasvnCdPfQXHrenDai+gyLARulZjZowCOj6fjT4pA==",
      "license": "MIT",
      "dependencies": {
        "@babel/helper-string-parser": "^7.29.7",
        "@babel/helper-validator-identifier": "^7.29.7"
      },
      "engines": {
        "node": ">=6.9.0"
      }
    },
    "node_modules/@esbuild/aix-ppc64": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/aix-ppc64/-/aix-ppc64-0.25.12.tgz",
      "integrity": "sha512-Hhmwd6CInZ3dwpuGTF8fJG6yoWmsToE+vYgD4nytZVxcu1ulHpUQRAB1UJ8+N1Am3Mz4+xOByoQoSZf4D+CpkA==",
      "cpu": [
        "ppc64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "aix"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/android-arm": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/android-arm/-/android-arm-0.25.12.tgz",
      "integrity": "sha512-VJ+sKvNA/GE7Ccacc9Cha7bpS8nyzVv0jdVgwNDaR4gDMC/2TTRc33Ip8qrNYUcpkOHUT5OZ0bUcNNVZQ9RLlg==",
      "cpu": [
        "arm"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "android"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/android-arm64": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/android-arm64/-/android-arm64-0.25.12.tgz",
      "integrity": "sha512-6AAmLG7zwD1Z159jCKPvAxZd4y/VTO0VkprYy+3N2FtJ8+BQWFXU+OxARIwA46c5tdD9SsKGZ/1ocqBS/gAKHg==",
      "cpu": [
        "arm64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "android"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/android-x64": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/android-x64/-/android-x64-0.25.12.tgz",
      "integrity": "sha512-5jbb+2hhDHx5phYR2By8GTWEzn6I9UqR11Kwf22iKbNpYrsmRB18aX/9ivc5cabcUiAT/wM+YIZ6SG9QO6a8kg==",
      "cpu": [
        "x64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "android"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/darwin-arm64": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/darwin-arm64/-/darwin-arm64-0.25.12.tgz",
      "integrity": "sha512-N3zl+lxHCifgIlcMUP5016ESkeQjLj/959RxxNYIthIg+CQHInujFuXeWbWMgnTo4cp5XVHqFPmpyu9J65C1Yg==",
      "cpu": [
        "arm64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "darwin"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/darwin-x64": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/darwin-x64/-/darwin-x64-0.25.12.tgz",
      "integrity": "sha512-HQ9ka4Kx21qHXwtlTUVbKJOAnmG1ipXhdWTmNXiPzPfWKpXqASVcWdnf2bnL73wgjNrFXAa3yYvBSd9pzfEIpA==",
      "cpu": [
        "x64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "darwin"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/freebsd-arm64": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/freebsd-arm64/-/freebsd-arm64-0.25.12.tgz",
      "integrity": "sha512-gA0Bx759+7Jve03K1S0vkOu5Lg/85dou3EseOGUes8flVOGxbhDDh/iZaoek11Y8mtyKPGF3vP8XhnkDEAmzeg==",
      "cpu": [
        "arm64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "freebsd"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/freebsd-x64": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/freebsd-x64/-/freebsd-x64-0.25.12.tgz",
      "integrity": "sha512-TGbO26Yw2xsHzxtbVFGEXBFH0FRAP7gtcPE7P5yP7wGy7cXK2oO7RyOhL5NLiqTlBh47XhmIUXuGciXEqYFfBQ==",
      "cpu": [
        "x64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "freebsd"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/linux-arm": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/linux-arm/-/linux-arm-0.25.12.tgz",
      "integrity": "sha512-lPDGyC1JPDou8kGcywY0YILzWlhhnRjdof3UlcoqYmS9El818LLfJJc3PXXgZHrHCAKs/Z2SeZtDJr5MrkxtOw==",
      "cpu": [
        "arm"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "linux"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/linux-arm64": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/linux-arm64/-/linux-arm64-0.25.12.tgz",
      "integrity": "sha512-8bwX7a8FghIgrupcxb4aUmYDLp8pX06rGh5HqDT7bB+8Rdells6mHvrFHHW2JAOPZUbnjUpKTLg6ECyzvas2AQ==",
      "cpu": [
        "arm64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "linux"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/linux-ia32": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/linux-ia32/-/linux-ia32-0.25.12.tgz",
      "integrity": "sha512-0y9KrdVnbMM2/vG8KfU0byhUN+EFCny9+8g202gYqSSVMonbsCfLjUO+rCci7pM0WBEtz+oK/PIwHkzxkyharA==",
      "cpu": [
        "ia32"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "linux"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/linux-loong64": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/linux-loong64/-/linux-loong64-0.25.12.tgz",
      "integrity": "sha512-h///Lr5a9rib/v1GGqXVGzjL4TMvVTv+s1DPoxQdz7l/AYv6LDSxdIwzxkrPW438oUXiDtwM10o9PmwS/6Z0Ng==",
      "cpu": [
        "loong64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "linux"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/linux-mips64el": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/linux-mips64el/-/linux-mips64el-0.25.12.tgz",
      "integrity": "sha512-iyRrM1Pzy9GFMDLsXn1iHUm18nhKnNMWscjmp4+hpafcZjrr2WbT//d20xaGljXDBYHqRcl8HnxbX6uaA/eGVw==",
      "cpu": [
        "mips64el"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "linux"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/linux-ppc64": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/linux-ppc64/-/linux-ppc64-0.25.12.tgz",
      "integrity": "sha512-9meM/lRXxMi5PSUqEXRCtVjEZBGwB7P/D4yT8UG/mwIdze2aV4Vo6U5gD3+RsoHXKkHCfSxZKzmDssVlRj1QQA==",
      "cpu": [
        "ppc64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "linux"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/linux-riscv64": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/linux-riscv64/-/linux-riscv64-0.25.12.tgz",
      "integrity": "sha512-Zr7KR4hgKUpWAwb1f3o5ygT04MzqVrGEGXGLnj15YQDJErYu/BGg+wmFlIDOdJp0PmB0lLvxFIOXZgFRrdjR0w==",
      "cpu": [
        "riscv64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "linux"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/linux-s390x": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/linux-s390x/-/linux-s390x-0.25.12.tgz",
      "integrity": "sha512-MsKncOcgTNvdtiISc/jZs/Zf8d0cl/t3gYWX8J9ubBnVOwlk65UIEEvgBORTiljloIWnBzLs4qhzPkJcitIzIg==",
      "cpu": [
        "s390x"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "linux"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/linux-x64": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/linux-x64/-/linux-x64-0.25.12.tgz",
      "integrity": "sha512-uqZMTLr/zR/ed4jIGnwSLkaHmPjOjJvnm6TVVitAa08SLS9Z0VM8wIRx7gWbJB5/J54YuIMInDquWyYvQLZkgw==",
      "cpu": [
        "x64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "linux"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/netbsd-arm64": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/netbsd-arm64/-/netbsd-arm64-0.25.12.tgz",
      "integrity": "sha512-xXwcTq4GhRM7J9A8Gv5boanHhRa/Q9KLVmcyXHCTaM4wKfIpWkdXiMog/KsnxzJ0A1+nD+zoecuzqPmCRyBGjg==",
      "cpu": [
        "arm64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "netbsd"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/netbsd-x64": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/netbsd-x64/-/netbsd-x64-0.25.12.tgz",
      "integrity": "sha512-Ld5pTlzPy3YwGec4OuHh1aCVCRvOXdH8DgRjfDy/oumVovmuSzWfnSJg+VtakB9Cm0gxNO9BzWkj6mtO1FMXkQ==",
      "cpu": [
        "x64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "netbsd"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/openbsd-arm64": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/openbsd-arm64/-/openbsd-arm64-0.25.12.tgz",
      "integrity": "sha512-fF96T6KsBo/pkQI950FARU9apGNTSlZGsv1jZBAlcLL1MLjLNIWPBkj5NlSz8aAzYKg+eNqknrUJ24QBybeR5A==",
      "cpu": [
        "arm64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "openbsd"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/openbsd-x64": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/openbsd-x64/-/openbsd-x64-0.25.12.tgz",
      "integrity": "sha512-MZyXUkZHjQxUvzK7rN8DJ3SRmrVrke8ZyRusHlP+kuwqTcfWLyqMOE3sScPPyeIXN/mDJIfGXvcMqCgYKekoQw==",
      "cpu": [
        "x64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "openbsd"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/openharmony-arm64": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/openharmony-arm64/-/openharmony-arm64-0.25.12.tgz",
      "integrity": "sha512-rm0YWsqUSRrjncSXGA7Zv78Nbnw4XL6/dzr20cyrQf7ZmRcsovpcRBdhD43Nuk3y7XIoW2OxMVvwuRvk9XdASg==",
      "cpu": [
        "arm64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "openharmony"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/sunos-x64": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/sunos-x64/-/sunos-x64-0.25.12.tgz",
      "integrity": "sha512-3wGSCDyuTHQUzt0nV7bocDy72r2lI33QL3gkDNGkod22EsYl04sMf0qLb8luNKTOmgF/eDEDP5BFNwoBKH441w==",
      "cpu": [
        "x64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "sunos"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/win32-arm64": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/win32-arm64/-/win32-arm64-0.25.12.tgz",
      "integrity": "sha512-rMmLrur64A7+DKlnSuwqUdRKyd3UE7oPJZmnljqEptesKM8wx9J8gx5u0+9Pq0fQQW8vqeKebwNXdfOyP+8Bsg==",
      "cpu": [
        "arm64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "win32"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/win32-ia32": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/win32-ia32/-/win32-ia32-0.25.12.tgz",
      "integrity": "sha512-HkqnmmBoCbCwxUKKNPBixiWDGCpQGVsrQfJoVGYLPT41XWF8lHuE5N6WhVia2n4o5QK5M4tYr21827fNhi4byQ==",
      "cpu": [
        "ia32"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "win32"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@esbuild/win32-x64": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/@esbuild/win32-x64/-/win32-x64-0.25.12.tgz",
      "integrity": "sha512-alJC0uCZpTFrSL0CCDjcgleBXPnCrEAhTBILpeAp7M/OFgoqtAetfBzX0xM00MUsVVPpVjlPuMbREqnZCXaTnA==",
      "cpu": [
        "x64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "win32"
      ],
      "engines": {
        "node": ">=18"
      }
    },
    "node_modules/@intlify/core-base": {
      "version": "10.0.8",
      "resolved": "https://registry.npmjs.org/@intlify/core-base/-/core-base-10.0.8.tgz",
      "integrity": "sha512-FoHslNWSoHjdUBLy35bpm9PV/0LVI/DSv9L6Km6J2ad8r/mm0VaGg06C40FqlE8u2ADcGUM60lyoU7Myo4WNZQ==",
      "license": "MIT",
      "dependencies": {
        "@intlify/message-compiler": "10.0.8",
        "@intlify/shared": "10.0.8"
      },
      "engines": {
        "node": ">= 16"
      },
      "funding": {
        "url": "https://github.com/sponsors/kazupon"
      }
    },
    "node_modules/@intlify/message-compiler": {
      "version": "10.0.8",
      "resolved": "https://registry.npmjs.org/@intlify/message-compiler/-/message-compiler-10.0.8.tgz",
      "integrity": "sha512-DV+sYXIkHVd5yVb2mL7br/NEUwzUoLBsMkV3H0InefWgmYa34NLZUvMCGi5oWX+Hqr2Y2qUxnVrnOWF4aBlgWg==",
      "license": "MIT",
      "dependencies": {
        "@intlify/shared": "10.0.8",
        "source-map-js": "^1.0.2"
      },
      "engines": {
        "node": ">= 16"
      },
      "funding": {
        "url": "https://github.com/sponsors/kazupon"
      }
    },
    "node_modules/@intlify/shared": {
      "version": "10.0.8",
      "resolved": "https://registry.npmjs.org/@intlify/shared/-/shared-10.0.8.tgz",
      "integrity": "sha512-BcmHpb5bQyeVNrptC3UhzpBZB/YHHDoEREOUERrmF2BRxsyOEuRrq+Z96C/D4+2KJb8kuHiouzAei7BXlG0YYw==",
      "license": "MIT",
      "engines": {
        "node": ">= 16"
      },
      "funding": {
        "url": "https://github.com/sponsors/kazupon"
      }
    },
    "node_modules/@jridgewell/sourcemap-codec": {
      "version": "1.5.5",
      "resolved": "https://registry.npmjs.org/@jridgewell/sourcemap-codec/-/sourcemap-codec-1.5.5.tgz",
      "integrity": "sha512-cYQ9310grqxueWbl+WuIUIaiUaDcj7WOq5fVhEljNVgRfOUhY9fy2zTvfoqWsnebh8Sl70VScFbICvJnLKB0Og==",
      "license": "MIT"
    },
    "node_modules/@primeuix/forms": {
      "version": "0.1.0",
      "resolved": "https://registry.npmjs.org/@primeuix/forms/-/forms-0.1.0.tgz",
      "integrity": "sha512-LctcQidb+B5PuvAFWH24YH/SIzmHlOabLHpaTeGY/k51iBv1WyCp+5w9JMYuMB/BplSvV0ZGySxQVkN5Azr/aQ==",
      "license": "MIT",
      "dependencies": {
        "@primeuix/utils": "^0.6.0"
      },
      "engines": {
        "node": ">=12.11.0"
      }
    },
    "node_modules/@primeuix/styled": {
      "version": "0.7.4",
      "resolved": "https://registry.npmjs.org/@primeuix/styled/-/styled-0.7.4.tgz",
      "integrity": "sha512-QSO/NpOQg8e9BONWRBx9y8VGMCMYz0J/uKfNJEya/RGEu7ARx0oYW0ugI1N3/KB1AAvyGxzKBzGImbwg0KUiOQ==",
      "license": "MIT",
      "dependencies": {
        "@primeuix/utils": "^0.6.1"
      },
      "engines": {
        "node": ">=12.11.0"
      }
    },
    "node_modules/@primeuix/styles": {
      "version": "2.0.3",
      "resolved": "https://registry.npmjs.org/@primeuix/styles/-/styles-2.0.3.tgz",
      "integrity": "sha512-2ykAB6BaHzR/6TwF8ShpJTsZrid6cVIEBVlookSdvOdmlWuevGu5vWOScgIwqWwlZcvkFYAGR/SUV3OHCTBMdw==",
      "license": "MIT",
      "dependencies": {
        "@primeuix/styled": "^0.7.4"
      }
    },
    "node_modules/@primeuix/themes": {
      "version": "1.2.5",
      "resolved": "https://registry.npmjs.org/@primeuix/themes/-/themes-1.2.5.tgz",
      "integrity": "sha512-n3YkwJrHQaEESc/D/A/iD815sxp8cKnmzscA6a8Tm8YvMtYU32eCahwLLe6h5rywghVwxASWuG36XBgISYOIjQ==",
      "license": "MIT",
      "dependencies": {
        "@primeuix/styled": "^0.7.3"
      }
    },
    "node_modules/@primeuix/utils": {
      "version": "0.6.4",
      "resolved": "https://registry.npmjs.org/@primeuix/utils/-/utils-0.6.4.tgz",
      "integrity": "sha512-pZ5f+vj7wSzRhC7KoEQRU5fvYAe+RP9+m39CTscZ3UywCD1Y2o6Fe1rRgklMPSkzUcty2jzkA0zMYkiJBD1hgg==",
      "license": "MIT",
      "engines": {
        "node": ">=12.11.0"
      }
    },
    "node_modules/@primevue/core": {
      "version": "4.5.5",
      "resolved": "https://registry.npmjs.org/@primevue/core/-/core-4.5.5.tgz",
      "integrity": "sha512-JpkXhq1ddc70JdsC3CC4dM+UbeeWuCW/8DpS9dNBfrOk824TLSlRlMEGFyVKqRMn5WPQvYLiy3xXfLQeNdSqhQ==",
      "license": "MIT",
      "dependencies": {
        "@primeuix/styled": "^0.7.4",
        "@primeuix/utils": "^0.6.2"
      },
      "engines": {
        "node": ">=12.11.0"
      },
      "peerDependencies": {
        "vue": "^3.5.0"
      }
    },
    "node_modules/@primevue/forms": {
      "version": "4.5.5",
      "resolved": "https://registry.npmjs.org/@primevue/forms/-/forms-4.5.5.tgz",
      "integrity": "sha512-LUeIt6oItwCZyBreQ7ycErE8aEjPmBWOTq1VPLhyaATcnzMBOZRIiwclaYk8s/tf5VYMqN9N4B80XnXoO7OdvQ==",
      "license": "MIT",
      "dependencies": {
        "@primeuix/forms": "^0.1.0",
        "@primeuix/utils": "^0.6.2",
        "@primevue/core": "4.5.5"
      },
      "engines": {
        "node": ">=12.11.0"
      }
    },
    "node_modules/@primevue/icons": {
      "version": "4.5.5",
      "resolved": "https://registry.npmjs.org/@primevue/icons/-/icons-4.5.5.tgz",
      "integrity": "sha512-eteOhTdAOXEYE9qW1AOrBBgDxQ2szHJxSkEK1XVdV2TKxGM5FQf03Ovms0VDyZTc16XBIgvwYjXJQS0BPbhPaA==",
      "license": "MIT",
      "dependencies": {
        "@primeuix/utils": "^0.6.2",
        "@primevue/core": "4.5.5"
      },
      "engines": {
        "node": ">=12.11.0"
      }
    },
    "node_modules/@rollup/rollup-android-arm-eabi": {
      "version": "4.61.1",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-android-arm-eabi/-/rollup-android-arm-eabi-4.61.1.tgz",
      "integrity": "sha512-JnBB8MdXj45cajvTuO5FmPlvFVJRQgvrz1uSEl3NwqFnReAPGwb8EanbGi4z2nRaqLzjJSv5/JmycoTKlRZxHA==",
      "cpu": [
        "arm"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "android"
      ]
    },
    "node_modules/@rollup/rollup-android-arm64": {
      "version": "4.61.1",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-android-arm64/-/rollup-android-arm64-4.61.1.tgz",
      "integrity": "sha512-Jx2g7iSjw4AOT0HDPHM9RV3GNjRXwybWtSFZiZAYUTjUwjVrYIwq3kBf+LnhqJlzXFAqTAh2F7IGI+O568exPw==",
      "cpu": [
        "arm64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "android"
      ]
    },
    "node_modules/@rollup/rollup-darwin-arm64": {
      "version": "4.61.1",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-darwin-arm64/-/rollup-darwin-arm64-4.61.1.tgz",
      "integrity": "sha512-0F1L/Z3Eqv8mT2n3dCpeO8GcTvHvVqkP5/t6DMsn0KzhYVcg+s7Ncl5DS8qjKYEeio6Az0Gt6nyBORay5qIlCA==",
      "cpu": [
        "arm64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "darwin"
      ]
    },
    "node_modules/@rollup/rollup-darwin-x64": {
      "version": "4.61.1",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-darwin-x64/-/rollup-darwin-x64-4.61.1.tgz",
      "integrity": "sha512-qLttcH871ujY4YcVfUSShhOw+CsoTatYz8gRbHO7Bb92QH059/P0y5do1KMs41fY0BpD2x4AJH/gID0zFiqVKQ==",
      "cpu": [
        "x64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "darwin"
      ]
    },
    "node_modules/@rollup/rollup-freebsd-arm64": {
      "version": "4.61.1",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-freebsd-arm64/-/rollup-freebsd-arm64-4.61.1.tgz",
      "integrity": "sha512-fUI4RapGE0Oh3mb8mgfvC1O2nU1RpDZUKnDQm3xB1Ipg7C2wTs5Kstz7G2uWK99a8S2yTMq8/P4uycwNa0nJyw==",
      "cpu": [
        "arm64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "freebsd"
      ]
    },
    "node_modules/@rollup/rollup-freebsd-x64": {
      "version": "4.61.1",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-freebsd-x64/-/rollup-freebsd-x64-4.61.1.tgz",
      "integrity": "sha512-H5YrdvJaDtI/U9/emrD4b++xkvp3y/JvOe4rizHbxvkyMfRS/CiRYdji+Pl8D0brEaNFWUh1drQxgAGIl6Xudw==",
      "cpu": [
        "x64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "freebsd"
      ]
    },
    "node_modules/@rollup/rollup-linux-arm-gnueabihf": {
      "version": "4.61.1",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-linux-arm-gnueabihf/-/rollup-linux-arm-gnueabihf-4.61.1.tgz",
      "integrity": "sha512-Q8CBCCQtDFrYtXoeUXSrnFXKOnyUhx6bz+SkL6A0E7V8kAiCJ5pamq1WtbfpVGhR5TSpXY6ak3avmDc5fHTyJA==",
      "cpu": [
        "arm"
      ],
      "dev": true,
      "libc": [
        "glibc"
      ],
      "license": "MIT",
      "optional": true,
      "os": [
        "linux"
      ]
    },
    "node_modules/@rollup/rollup-linux-arm-musleabihf": {
      "version": "4.61.1",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-linux-arm-musleabihf/-/rollup-linux-arm-musleabihf-4.61.1.tgz",
      "integrity": "sha512-nwnhk1581l0FBVellGcVCAT0Oi06onEA3WB53sf01VO3I0UPBkMH9sXONYME2K0ovXcNayJfNtHfm6mpJElatQ==",
      "cpu": [
        "arm"
      ],
      "dev": true,
      "libc": [
        "musl"
      ],
      "license": "MIT",
      "optional": true,
      "os": [
        "linux"
      ]
    },
    "node_modules/@rollup/rollup-linux-arm64-gnu": {
      "version": "4.61.1",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-linux-arm64-gnu/-/rollup-linux-arm64-gnu-4.61.1.tgz",
      "integrity": "sha512-x5Xr49hwt3hdW75UOZm3395YwwzPyauktslv29KpWL/T+vVAzoT3azLcTWv0eMciBNrx+DYjH4paehHoLpPvpg==",
      "cpu": [
        "arm64"
      ],
      "dev": true,
      "libc": [
        "glibc"
      ],
      "license": "MIT",
      "optional": true,
      "os": [
        "linux"
      ]
    },
    "node_modules/@rollup/rollup-linux-arm64-musl": {
      "version": "4.61.1",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-linux-arm64-musl/-/rollup-linux-arm64-musl-4.61.1.tgz",
      "integrity": "sha512-unMS3H73DpaoPyyEVPjGKleM/s0mkmsauTENpw4INQY8y4+IuLNjkueQ5QCtC0D3N38Y38yhAU8OoZ20S2Tm6w==",
      "cpu": [
        "arm64"
      ],
      "dev": true,
      "libc": [
        "musl"
      ],
      "license": "MIT",
      "optional": true,
      "os": [
        "linux"
      ]
    },
    "node_modules/@rollup/rollup-linux-loong64-gnu": {
      "version": "4.61.1",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-linux-loong64-gnu/-/rollup-linux-loong64-gnu-4.61.1.tgz",
      "integrity": "sha512-zNZzGRnAhwjFEYmvphJRV5XaQGjs62cCmeYYHUT//NbvEnHauw+I85nGG+SiVg5ld4GX8D1IbKIX+ozITQnhMQ==",
      "cpu": [
        "loong64"
      ],
      "dev": true,
      "libc": [
        "glibc"
      ],
      "license": "MIT",
      "optional": true,
      "os": [
        "linux"
      ]
    },
    "node_modules/@rollup/rollup-linux-loong64-musl": {
      "version": "4.61.1",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-linux-loong64-musl/-/rollup-linux-loong64-musl-4.61.1.tgz",
      "integrity": "sha512-LdpWGL8X209B2SIvWjqlc8VZgM6PKfontSerGepuldQmHYrAOtnMCXeJkxXGbC+PPZVOuu5czJo7fNV6aeW8rQ==",
      "cpu": [
        "loong64"
      ],
      "dev": true,
      "libc": [
        "musl"
      ],
      "license": "MIT",
      "optional": true,
      "os": [
        "linux"
      ]
    },
    "node_modules/@rollup/rollup-linux-ppc64-gnu": {
      "version": "4.61.1",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-linux-ppc64-gnu/-/rollup-linux-ppc64-gnu-4.61.1.tgz",
      "integrity": "sha512-EC5kTtNaNGOmbMGqar8dvJy6y/hg99GAwjfBz++pxZhQATXGcRjd6c5en5wcbru0vkRmiMGsQKdMJOOf6sza4g==",
      "cpu": [
        "ppc64"
      ],
      "dev": true,
      "libc": [
        "glibc"
      ],
      "license": "MIT",
      "optional": true,
      "os": [
        "linux"
      ]
    },
    "node_modules/@rollup/rollup-linux-ppc64-musl": {
      "version": "4.61.1",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-linux-ppc64-musl/-/rollup-linux-ppc64-musl-4.61.1.tgz",
      "integrity": "sha512-8hiwp6D4acEcNK78I4rP0/XtS1sknWIAMJBPdR4l6zUtyTm5KiTDr5bXmWt4foY7nAN7AThDHgkLIEZOWKbzWw==",
      "cpu": [
        "ppc64"
      ],
      "dev": true,
      "libc": [
        "musl"
      ],
      "license": "MIT",
      "optional": true,
      "os": [
        "linux"
      ]
    },
    "node_modules/@rollup/rollup-linux-riscv64-gnu": {
      "version": "4.61.1",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-linux-riscv64-gnu/-/rollup-linux-riscv64-gnu-4.61.1.tgz",
      "integrity": "sha512-10dh/h/BqA7DuMPWSxkR8uks18FRwnwOEqr5zOTEl+NOwP/OMzKX8OFR/Of9xxDA7D5qef1Nzar5WDD2kCCr1g==",
      "cpu": [
        "riscv64"
      ],
      "dev": true,
      "libc": [
        "glibc"
      ],
      "license": "MIT",
      "optional": true,
      "os": [
        "linux"
      ]
    },
    "node_modules/@rollup/rollup-linux-riscv64-musl": {
      "version": "4.61.1",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-linux-riscv64-musl/-/rollup-linux-riscv64-musl-4.61.1.tgz",
      "integrity": "sha512-YKJ5lg35DP17gcAOggnihe+APw9HLyj1Xn7gsmGumBJAUDa6NGXNixJzmkWLhcK9TOuuyQjdamzvJefkO7qHZQ==",
      "cpu": [
        "riscv64"
      ],
      "dev": true,
      "libc": [
        "musl"
      ],
      "license": "MIT",
      "optional": true,
      "os": [
        "linux"
      ]
    },
    "node_modules/@rollup/rollup-linux-s390x-gnu": {
      "version": "4.61.1",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-linux-s390x-gnu/-/rollup-linux-s390x-gnu-4.61.1.tgz",
      "integrity": "sha512-Mlil5G2Jj6a7B3LWGctg+XPL9vdXYuzCtNXfxOQ0nPjc2m6ueUktocPGH9bnAM0bNRKb/bAWTujUU7IJQdQA+g==",
      "cpu": [
        "s390x"
      ],
      "dev": true,
      "libc": [
        "glibc"
      ],
      "license": "MIT",
      "optional": true,
      "os": [
        "linux"
      ]
    },
    "node_modules/@rollup/rollup-linux-x64-gnu": {
      "version": "4.61.1",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-linux-x64-gnu/-/rollup-linux-x64-gnu-4.61.1.tgz",
      "integrity": "sha512-bVWIOIk6pV01p4CdUbPP7CJ/434z+OooYjDuFcR+44N35YvKUC66G8MGnvcWx5mWKW3g61J+t74l3Kj15Kwn2Q==",
      "cpu": [
        "x64"
      ],
      "dev": true,
      "libc": [
        "glibc"
      ],
      "license": "MIT",
      "optional": true,
      "os": [
        "linux"
      ]
    },
    "node_modules/@rollup/rollup-linux-x64-musl": {
      "version": "4.61.1",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-linux-x64-musl/-/rollup-linux-x64-musl-4.61.1.tgz",
      "integrity": "sha512-qy5pBvZbqNFheBz61R1rzsezjm0J7O2oNGoWtGoY89SZYLUfxAJTBAqDChqAIdB4rCiIbi9nF7yZ83GnNiLwSw==",
      "cpu": [
        "x64"
      ],
      "dev": true,
      "libc": [
        "musl"
      ],
      "license": "MIT",
      "optional": true,
      "os": [
        "linux"
      ]
    },
    "node_modules/@rollup/rollup-openbsd-x64": {
      "version": "4.61.1",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-openbsd-x64/-/rollup-openbsd-x64-4.61.1.tgz",
      "integrity": "sha512-E83TXjI4zm0+5f2qO+UOudaCYIhYwpJ5jq6YCZNIZ+6CbfhKrkAGezeiASBL9ElxAxFsRS9ZhESv8mfnj6TKeg==",
      "cpu": [
        "x64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "openbsd"
      ]
    },
    "node_modules/@rollup/rollup-openharmony-arm64": {
      "version": "4.61.1",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-openharmony-arm64/-/rollup-openharmony-arm64-4.61.1.tgz",
      "integrity": "sha512-fbWnKqVkjrJN38vNe3ahkbk6iejS/3b0Nt7EEtPpE6RBacZcGXNKbzfHN3GUUlXOPghUg0j6XUGrtjX9z1sIvA==",
      "cpu": [
        "arm64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "openharmony"
      ]
    },
    "node_modules/@rollup/rollup-win32-arm64-msvc": {
      "version": "4.61.1",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-win32-arm64-msvc/-/rollup-win32-arm64-msvc-4.61.1.tgz",
      "integrity": "sha512-ArMl38iVAbk0New1ogihQNY6iphLi4ZaRsa037gUzv5yeKPY8TD3Dmy4x2RNC1VztU/uqm+G+/RwFrSka3Oy2g==",
      "cpu": [
        "arm64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "win32"
      ]
    },
    "node_modules/@rollup/rollup-win32-ia32-msvc": {
      "version": "4.61.1",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-win32-ia32-msvc/-/rollup-win32-ia32-msvc-4.61.1.tgz",
      "integrity": "sha512-0mYtjHS9ucAbcATycCNK9IGBk/cCe/ma7EmSLGZdsxnOA8cjRIyU04wDpVAD9NiOfLUR9KTxdiO53uOkherqjQ==",
      "cpu": [
        "ia32"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "win32"
      ]
    },
    "node_modules/@rollup/rollup-win32-x64-gnu": {
      "version": "4.61.1",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-win32-x64-gnu/-/rollup-win32-x64-gnu-4.61.1.tgz",
      "integrity": "sha512-gK1iCEPfpoSG9wfBihXxvBMi8ZfcWffYkEsC/Eih+iFENTaewvNcrEQ69lIOWYO5pePHKLHHO7nq5AILGO/HQQ==",
      "cpu": [
        "x64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "win32"
      ]
    },
    "node_modules/@rollup/rollup-win32-x64-msvc": {
      "version": "4.61.1",
      "resolved": "https://registry.npmjs.org/@rollup/rollup-win32-x64-msvc/-/rollup-win32-x64-msvc-4.61.1.tgz",
      "integrity": "sha512-X+zaP2x+j4RXGfbp/seSoRHWnPxzApilDszisZxbYH5C/jTxFhCtDNdPGZb9lJyYPs24wGxruPF7Y+sIXt9Gzw==",
      "cpu": [
        "x64"
      ],
      "dev": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "win32"
      ]
    },
    "node_modules/@types/estree": {
      "version": "1.0.9",
      "resolved": "https://registry.npmjs.org/@types/estree/-/estree-1.0.9.tgz",
      "integrity": "sha512-GhdPgy1el4/ImP05X05Uw4cw2/M93BCUmnEvWZNStlCzEKME4Fkk+YpoA5OiHNQmoS7Cafb8Xa3Pya8m1Qrzeg==",
      "dev": true,
      "license": "MIT"
    },
    "node_modules/@vitejs/plugin-vue": {
      "version": "5.2.4",
      "resolved": "https://registry.npmjs.org/@vitejs/plugin-vue/-/plugin-vue-5.2.4.tgz",
      "integrity": "sha512-7Yx/SXSOcQq5HiiV3orevHUFn+pmMB4cgbEkDYgnkUWb0WfeQ/wa2yFv6D5ICiCQOVpjA7vYDXrC7AGO8yjDHA==",
      "dev": true,
      "license": "MIT",
      "engines": {
        "node": "^18.0.0 || >=20.0.0"
      },
      "peerDependencies": {
        "vite": "^5.0.0 || ^6.0.0",
        "vue": "^3.2.25"
      }
    },
    "node_modules/@volar/language-core": {
      "version": "2.4.15",
      "resolved": "https://registry.npmjs.org/@volar/language-core/-/language-core-2.4.15.tgz",
      "integrity": "sha512-3VHw+QZU0ZG9IuQmzT68IyN4hZNd9GchGPhbD9+pa8CVv7rnoOZwo7T8weIbrRmihqy3ATpdfXFnqRrfPVK6CA==",
      "dev": true,
      "license": "MIT",
      "dependencies": {
        "@volar/source-map": "2.4.15"
      }
    },
    "node_modules/@volar/source-map": {
      "version": "2.4.15",
      "resolved": "https://registry.npmjs.org/@volar/source-map/-/source-map-2.4.15.tgz",
      "integrity": "sha512-CPbMWlUN6hVZJYGcU/GSoHu4EnCHiLaXI9n8c9la6RaI9W5JHX+NqG+GSQcB0JdC2FIBLdZJwGsfKyBB71VlTg==",
      "dev": true,
      "license": "MIT"
    },
    "node_modules/@volar/typescript": {
      "version": "2.4.15",
      "resolved": "https://registry.npmjs.org/@volar/typescript/-/typescript-2.4.15.tgz",
      "integrity": "sha512-2aZ8i0cqPGjXb4BhkMsPYDkkuc2ZQ6yOpqwAuNwUoncELqoy5fRgOQtLR9gB0g902iS0NAkvpIzs27geVyVdPg==",
      "dev": true,
      "license": "MIT",
      "dependencies": {
        "@volar/language-core": "2.4.15",
        "path-browserify": "^1.0.1",
        "vscode-uri": "^3.0.8"
      }
    },
    "node_modules/@vue/compiler-core": {
      "version": "3.5.35",
      "resolved": "https://registry.npmjs.org/@vue/compiler-core/-/compiler-core-3.5.35.tgz",
      "integrity": "sha512-BUmHaR1J+O+CKZ9uJucdVTEr1LHsdyvv7vG3eNRhK3CczEHeMd/LtsHAuD7PbrxvI2envCY2v7HI1vC1aBRzKw==",
      "license": "MIT",
      "dependencies": {
        "@babel/parser": "^7.29.3",
        "@vue/shared": "3.5.35",
        "entities": "^7.0.1",
        "estree-walker": "^2.0.2",
        "source-map-js": "^1.2.1"
      }
    },
    "node_modules/@vue/compiler-dom": {
      "version": "3.5.35",
      "resolved": "https://registry.npmjs.org/@vue/compiler-dom/-/compiler-dom-3.5.35.tgz",
      "integrity": "sha512-k+bprkXxuqhVajgTx5mUHuir7TwQzUKOWR40ng1ncAqQRPnrLngGGgqVEEhOnTMlc8btHYVKmrP8s5Qyg0hvYA==",
      "license": "MIT",
      "dependencies": {
        "@vue/compiler-core": "3.5.35",
        "@vue/shared": "3.5.35"
      }
    },
    "node_modules/@vue/compiler-sfc": {
      "version": "3.5.35",
      "resolved": "https://registry.npmjs.org/@vue/compiler-sfc/-/compiler-sfc-3.5.35.tgz",
      "integrity": "sha512-G5VPMcXTSywXBgtFOZOnHKBxKSrwXUcvY1iaF5/hRcy7t0J6CH/d8ha9F4nzi00Fax1eLV0QHM7v4mQu68jydw==",
      "license": "MIT",
      "dependencies": {
        "@babel/parser": "^7.29.3",
        "@vue/compiler-core": "3.5.35",
        "@vue/compiler-dom": "3.5.35",
        "@vue/compiler-ssr": "3.5.35",
        "@vue/shared": "3.5.35",
        "estree-walker": "^2.0.2",
        "magic-string": "^0.30.21",
        "postcss": "^8.5.15",
        "source-map-js": "^1.2.1"
      }
    },
    "node_modules/@vue/compiler-ssr": {
      "version": "3.5.35",
      "resolved": "https://registry.npmjs.org/@vue/compiler-ssr/-/compiler-ssr-3.5.35.tgz",
      "integrity": "sha512-rGhAeXgdM7/ffTJGXT69rCCdTmjDewnFuUZfBQQHTdcEBeWdT5HCGY60y2ytLJr9/Dsu7IntUi5z/w0h6Rjnzw==",
      "license": "MIT",
      "dependencies": {
        "@vue/compiler-dom": "3.5.35",
        "@vue/shared": "3.5.35"
      }
    },
    "node_modules/@vue/compiler-vue2": {
      "version": "2.7.16",
      "resolved": "https://registry.npmjs.org/@vue/compiler-vue2/-/compiler-vue2-2.7.16.tgz",
      "integrity": "sha512-qYC3Psj9S/mfu9uVi5WvNZIzq+xnXMhOwbTFKKDD7b1lhpnn71jXSFdTQ+WsIEk0ONCd7VV2IMm7ONl6tbQ86A==",
      "dev": true,
      "license": "MIT",
      "dependencies": {
        "de-indent": "^1.0.2",
        "he": "^1.2.0"
      }
    },
    "node_modules/@vue/devtools-api": {
      "version": "6.6.4",
      "resolved": "https://registry.npmjs.org/@vue/devtools-api/-/devtools-api-6.6.4.tgz",
      "integrity": "sha512-sGhTPMuXqZ1rVOk32RylztWkfXTRhuS7vgAKv0zjqk8gbsHkJ7xfFf+jbySxt7tWObEJwyKaHMikV/WGDiQm8g==",
      "license": "MIT"
    },
    "node_modules/@vue/language-core": {
      "version": "2.2.12",
      "resolved": "https://registry.npmjs.org/@vue/language-core/-/language-core-2.2.12.tgz",
      "integrity": "sha512-IsGljWbKGU1MZpBPN+BvPAdr55YPkj2nB/TBNGNC32Vy2qLG25DYu/NBN2vNtZqdRbTRjaoYrahLrToim2NanA==",
      "dev": true,
      "license": "MIT",
      "dependencies": {
        "@volar/language-core": "2.4.15",
        "@vue/compiler-dom": "^3.5.0",
        "@vue/compiler-vue2": "^2.7.16",
        "@vue/shared": "^3.5.0",
        "alien-signals": "^1.0.3",
        "minimatch": "^9.0.3",
        "muggle-string": "^0.4.1",
        "path-browserify": "^1.0.1"
      },
      "peerDependencies": {
        "typescript": "*"
      },
      "peerDependenciesMeta": {
        "typescript": {
          "optional": true
        }
      }
    },
    "node_modules/@vue/reactivity": {
      "version": "3.5.35",
      "resolved": "https://registry.npmjs.org/@vue/reactivity/-/reactivity-3.5.35.tgz",
      "integrity": "sha512-tVc+SsHConvh/Lz64qq1pP3rYArBmK42xonovEcxY74SQtvctZodG/zhq54P5dr38cVuw25d27cPNRdlMidpGQ==",
      "license": "MIT",
      "dependencies": {
        "@vue/shared": "3.5.35"
      }
    },
    "node_modules/@vue/runtime-core": {
      "version": "3.5.35",
      "resolved": "https://registry.npmjs.org/@vue/runtime-core/-/runtime-core-3.5.35.tgz",
      "integrity": "sha512-A/xFNX9loIcWDygeQuNCfKuh0CoYBzxhqEMNah5TSFg9Z53DrFYEN2qi5CU9necjM1OWYegYREUTHmXTmhfXtg==",
      "license": "MIT",
      "dependencies": {
        "@vue/reactivity": "3.5.35",
        "@vue/shared": "3.5.35"
      }
    },
    "node_modules/@vue/runtime-dom": {
      "version": "3.5.35",
      "resolved": "https://registry.npmjs.org/@vue/runtime-dom/-/runtime-dom-3.5.35.tgz",
      "integrity": "sha512-odrJ1C391dbGnyDRh8U+rnP7J2amIEzfmRk5vXy7xi3aZhEXofTvpi0T4HJb6jlNqQZTNPR5MPHSB3RHNkIORA==",
      "license": "MIT",
      "dependencies": {
        "@vue/reactivity": "3.5.35",
        "@vue/runtime-core": "3.5.35",
        "@vue/shared": "3.5.35",
        "csstype": "^3.2.3"
      }
    },
    "node_modules/@vue/server-renderer": {
      "version": "3.5.35",
      "resolved": "https://registry.npmjs.org/@vue/server-renderer/-/server-renderer-3.5.35.tgz",
      "integrity": "sha512-NkebSOYdB97wi8OQcO3HqzZSlymJi/aWsN/7h74OSVhRTm6qGs3Jp3e0rCXynmWwSlKeRrnlIug+ilYoHBmQDA==",
      "license": "MIT",
      "dependencies": {
        "@vue/compiler-ssr": "3.5.35",
        "@vue/shared": "3.5.35"
      },
      "peerDependencies": {
        "vue": "3.5.35"
      }
    },
    "node_modules/@vue/shared": {
      "version": "3.5.35",
      "resolved": "https://registry.npmjs.org/@vue/shared/-/shared-3.5.35.tgz",
      "integrity": "sha512-zSbjL7gRXwks2ZQLRGCajBtBXEOXW9Ddhn/HvSdrGkE2dqGnumzW8XtusRrxrE9LvqtiqDXQ+A60Hp6mvdYxfA==",
      "license": "MIT"
    },
    "node_modules/alien-signals": {
      "version": "1.0.13",
      "resolved": "https://registry.npmjs.org/alien-signals/-/alien-signals-1.0.13.tgz",
      "integrity": "sha512-OGj9yyTnJEttvzhTUWuscOvtqxq5vrhF7vL9oS0xJ2mK0ItPYP1/y+vCFebfxoEyAz0++1AIwJ5CMr+Fk3nDmg==",
      "dev": true,
      "license": "MIT"
    },
    "node_modules/balanced-match": {
      "version": "1.0.2",
      "resolved": "https://registry.npmjs.org/balanced-match/-/balanced-match-1.0.2.tgz",
      "integrity": "sha512-3oSeUO0TMV67hN1AmbXsK4yaqU7tjiHlbxRDZOpH0KW9+CeX4bRAaX0Anxt0tx2MrpRpWwQaPwIlISEJhYU5Pw==",
      "dev": true,
      "license": "MIT"
    },
    "node_modules/brace-expansion": {
      "version": "2.1.1",
      "resolved": "https://registry.npmjs.org/brace-expansion/-/brace-expansion-2.1.1.tgz",
      "integrity": "sha512-WR1cURNjuvBLMZBMbqM0UoE+WAfdUcEV1ccD8PVBVOI+Z3ND4+SZbN8RsfT2bMuG1qwz5RFvPukSZm5fF2D5eA==",
      "dev": true,
      "license": "MIT",
      "dependencies": {
        "balanced-match": "^1.0.0"
      }
    },
    "node_modules/csstype": {
      "version": "3.2.3",
      "resolved": "https://registry.npmjs.org/csstype/-/csstype-3.2.3.tgz",
      "integrity": "sha512-z1HGKcYy2xA8AGQfwrn0PAy+PB7X/GSj3UVJW9qKyn43xWa+gl5nXmU4qqLMRzWVLFC8KusUX8T/0kCiOYpAIQ==",
      "license": "MIT"
    },
    "node_modules/de-indent": {
      "version": "1.0.2",
      "resolved": "https://registry.npmjs.org/de-indent/-/de-indent-1.0.2.tgz",
      "integrity": "sha512-e/1zu3xH5MQryN2zdVaF0OrdNLUbvWxzMbi+iNA6Bky7l1RoP8a2fIbRocyHclXt/arDrrR6lL3TqFD9pMQTsg==",
      "dev": true,
      "license": "MIT"
    },
    "node_modules/entities": {
      "version": "7.0.1",
      "resolved": "https://registry.npmjs.org/entities/-/entities-7.0.1.tgz",
      "integrity": "sha512-TWrgLOFUQTH994YUyl1yT4uyavY5nNB5muff+RtWaqNVCAK408b5ZnnbNAUEWLTCpum9w6arT70i1XdQ4UeOPA==",
      "license": "BSD-2-Clause",
      "engines": {
        "node": ">=0.12"
      },
      "funding": {
        "url": "https://github.com/fb55/entities?sponsor=1"
      }
    },
    "node_modules/esbuild": {
      "version": "0.25.12",
      "resolved": "https://registry.npmjs.org/esbuild/-/esbuild-0.25.12.tgz",
      "integrity": "sha512-bbPBYYrtZbkt6Os6FiTLCTFxvq4tt3JKall1vRwshA3fdVztsLAatFaZobhkBC8/BrPetoa0oksYoKXoG4ryJg==",
      "dev": true,
      "hasInstallScript": true,
      "license": "MIT",
      "bin": {
        "esbuild": "bin/esbuild"
      },
      "engines": {
        "node": ">=18"
      },
      "optionalDependencies": {
        "@esbuild/aix-ppc64": "0.25.12",
        "@esbuild/android-arm": "0.25.12",
        "@esbuild/android-arm64": "0.25.12",
        "@esbuild/android-x64": "0.25.12",
        "@esbuild/darwin-arm64": "0.25.12",
        "@esbuild/darwin-x64": "0.25.12",
        "@esbuild/freebsd-arm64": "0.25.12",
        "@esbuild/freebsd-x64": "0.25.12",
        "@esbuild/linux-arm": "0.25.12",
        "@esbuild/linux-arm64": "0.25.12",
        "@esbuild/linux-ia32": "0.25.12",
        "@esbuild/linux-loong64": "0.25.12",
        "@esbuild/linux-mips64el": "0.25.12",
        "@esbuild/linux-ppc64": "0.25.12",
        "@esbuild/linux-riscv64": "0.25.12",
        "@esbuild/linux-s390x": "0.25.12",
        "@esbuild/linux-x64": "0.25.12",
        "@esbuild/netbsd-arm64": "0.25.12",
        "@esbuild/netbsd-x64": "0.25.12",
        "@esbuild/openbsd-arm64": "0.25.12",
        "@esbuild/openbsd-x64": "0.25.12",
        "@esbuild/openharmony-arm64": "0.25.12",
        "@esbuild/sunos-x64": "0.25.12",
        "@esbuild/win32-arm64": "0.25.12",
        "@esbuild/win32-ia32": "0.25.12",
        "@esbuild/win32-x64": "0.25.12"
      }
    },
    "node_modules/estree-walker": {
      "version": "2.0.2",
      "resolved": "https://registry.npmjs.org/estree-walker/-/estree-walker-2.0.2.tgz",
      "integrity": "sha512-Rfkk/Mp/DL7JVje3u18FxFujQlTNR2q6QfMSMB7AvCBx91NGj/ba3kCfza0f6dVDbw7YlRf/nDrn7pQrCCyQ/w==",
      "license": "MIT"
    },
    "node_modules/fdir": {
      "version": "6.5.0",
      "resolved": "https://registry.npmjs.org/fdir/-/fdir-6.5.0.tgz",
      "integrity": "sha512-tIbYtZbucOs0BRGqPJkshJUYdL+SDH7dVM8gjy+ERp3WAUjLEFJE+02kanyHtwjWOnwrKYBiwAmM0p4kLJAnXg==",
      "dev": true,
      "license": "MIT",
      "engines": {
        "node": ">=12.0.0"
      },
      "peerDependencies": {
        "picomatch": "^3 || ^4"
      },
      "peerDependenciesMeta": {
        "picomatch": {
          "optional": true
        }
      }
    },
    "node_modules/fsevents": {
      "version": "2.3.3",
      "resolved": "https://registry.npmjs.org/fsevents/-/fsevents-2.3.3.tgz",
      "integrity": "sha512-5xoDfX+fL7faATnagmWPpbFtwh/R77WmMMqqHGS65C3vvB0YHrgF+B1YmZ3441tMj5n63k0212XNoJwzlhffQw==",
      "dev": true,
      "hasInstallScript": true,
      "license": "MIT",
      "optional": true,
      "os": [
        "darwin"
      ],
      "engines": {
        "node": "^8.16.0 || ^10.6.0 || >=11.0.0"
      }
    },
    "node_modules/he": {
      "version": "1.2.0",
      "resolved": "https://registry.npmjs.org/he/-/he-1.2.0.tgz",
      "integrity": "sha512-F/1DnUGPopORZi0ni+CvrCgHQ5FyEAHRLSApuYWMmrbSwoN2Mn/7k+Gl38gJnR7yyDZk6WLXwiGod1JOWNDKGw==",
      "dev": true,
      "license": "MIT",
      "bin": {
        "he": "bin/he"
      }
    },
    "node_modules/magic-string": {
      "version": "0.30.21",
      "resolved": "https://registry.npmjs.org/magic-string/-/magic-string-0.30.21.tgz",
      "integrity": "sha512-vd2F4YUyEXKGcLHoq+TEyCjxueSeHnFxyyjNp80yg0XV4vUhnDer/lvvlqM/arB5bXQN5K2/3oinyCRyx8T2CQ==",
      "license": "MIT",
      "dependencies": {
        "@jridgewell/sourcemap-codec": "^1.5.5"
      }
    },
    "node_modules/minimatch": {
      "version": "9.0.9",
      "resolved": "https://registry.npmjs.org/minimatch/-/minimatch-9.0.9.tgz",
      "integrity": "sha512-OBwBN9AL4dqmETlpS2zasx+vTeWclWzkblfZk7KTA5j3jeOONz/tRCnZomUyvNg83wL5Zv9Ss6HMJXAgL8R2Yg==",
      "dev": true,
      "license": "ISC",
      "dependencies": {
        "brace-expansion": "^2.0.2"
      },
      "engines": {
        "node": ">=16 || 14 >=14.17"
      },
      "funding": {
        "url": "https://github.com/sponsors/isaacs"
      }
    },
    "node_modules/muggle-string": {
      "version": "0.4.1",
      "resolved": "https://registry.npmjs.org/muggle-string/-/muggle-string-0.4.1.tgz",
      "integrity": "sha512-VNTrAak/KhO2i8dqqnqnAHOa3cYBwXEZe9h+D5h/1ZqFSTEFHdM65lR7RoIqq3tBBYavsOXV84NoHXZ0AkPyqQ==",
      "dev": true,
      "license": "MIT"
    },
    "node_modules/nanoid": {
      "version": "3.3.12",
      "resolved": "https://registry.npmjs.org/nanoid/-/nanoid-3.3.12.tgz",
      "integrity": "sha512-ZB9RH/39qpq5Vu6Y+NmUaFhQR6pp+M2Xt76XBnEwDaGcVAqhlvxrl3B2bKS5D3NH3QR76v3aSrKaF/Kiy7lEtQ==",
      "funding": [
        {
          "type": "github",
          "url": "https://github.com/sponsors/ai"
        }
      ],
      "license": "MIT",
      "bin": {
        "nanoid": "bin/nanoid.cjs"
      },
      "engines": {
        "node": "^10 || ^12 || ^13.7 || ^14 || >=15.0.1"
      }
    },
    "node_modules/path-browserify": {
      "version": "1.0.1",
      "resolved": "https://registry.npmjs.org/path-browserify/-/path-browserify-1.0.1.tgz",
      "integrity": "sha512-b7uo2UCUOYZcnF/3ID0lulOJi/bafxa1xPe7ZPsammBSpjSWQkjNxlt635YGS2MiR9GjvuXCtz2emr3jbsz98g==",
      "dev": true,
      "license": "MIT"
    },
    "node_modules/picocolors": {
      "version": "1.1.1",
      "resolved": "https://registry.npmjs.org/picocolors/-/picocolors-1.1.1.tgz",
      "integrity": "sha512-xceH2snhtb5M9liqDsmEw56le376mTZkEX/jEb/RxNFyegNul7eNslCXP9FDj/Lcu0X8KEyMceP2ntpaHrDEVA==",
      "license": "ISC"
    },
    "node_modules/picomatch": {
      "version": "4.0.4",
      "resolved": "https://registry.npmjs.org/picomatch/-/picomatch-4.0.4.tgz",
      "integrity": "sha512-QP88BAKvMam/3NxH6vj2o21R6MjxZUAd6nlwAS/pnGvN9IVLocLHxGYIzFhg6fUQ+5th6P4dv4eW9jX3DSIj7A==",
      "dev": true,
      "license": "MIT",
      "engines": {
        "node": ">=12"
      },
      "funding": {
        "url": "https://github.com/sponsors/jonschlinkert"
      }
    },
    "node_modules/postcss": {
      "version": "8.5.15",
      "resolved": "https://registry.npmjs.org/postcss/-/postcss-8.5.15.tgz",
      "integrity": "sha512-FfR8sjd4em2T6fb3I2MwAJU7HWVMr9zba+enmQeeWFfCbm+UOC/0X4DS8XtpUTMwWMGbjKYP7xjfNekzyGmB3A==",
      "funding": [
        {
          "type": "opencollective",
          "url": "https://opencollective.com/postcss/"
        },
        {
          "type": "tidelift",
          "url": "https://tidelift.com/funding/github/npm/postcss"
        },
        {
          "type": "github",
          "url": "https://github.com/sponsors/ai"
        }
      ],
      "license": "MIT",
      "dependencies": {
        "nanoid": "^3.3.12",
        "picocolors": "^1.1.1",
        "source-map-js": "^1.2.1"
      },
      "engines": {
        "node": "^10 || ^12 || >=14"
      }
    },
    "node_modules/primeicons": {
      "version": "7.0.0",
      "resolved": "https://registry.npmjs.org/primeicons/-/primeicons-7.0.0.tgz",
      "integrity": "sha512-jK3Et9UzwzTsd6tzl2RmwrVY/b8raJ3QZLzoDACj+oTJ0oX7L9Hy+XnVwgo4QVKlKpnP/Ur13SXV/pVh4LzaDw==",
      "license": "MIT"
    },
    "node_modules/primevue": {
      "version": "4.5.5",
      "resolved": "https://registry.npmjs.org/primevue/-/primevue-4.5.5.tgz",
      "integrity": "sha512-Kv5REIewCdP806QaoU+4nBXfmpzOGFKkZ9qH4KsL6MjiAQVc4PUzypt8erl4r3Vzh3nr3aWZIxkxYRRsLGiX2A==",
      "license": "MIT",
      "dependencies": {
        "@primeuix/styled": "^0.7.4",
        "@primeuix/styles": "^2.0.3",
        "@primeuix/utils": "^0.6.2",
        "@primevue/core": "4.5.5",
        "@primevue/icons": "4.5.5"
      },
      "engines": {
        "node": ">=12.11.0"
      }
    },
    "node_modules/rollup": {
      "version": "4.61.1",
      "resolved": "https://registry.npmjs.org/rollup/-/rollup-4.61.1.tgz",
      "integrity": "sha512-I4KW6iuRpuu2uHBLraZ1wNZe0DP7lnRha+VJ9tNaYVaVgKhW0aI3h4RYnoRPeql0flHm/Co55b7snEDcOfOJrA==",
      "dev": true,
      "license": "MIT",
      "dependencies": {
        "@types/estree": "1.0.9"
      },
      "bin": {
        "rollup": "dist/bin/rollup"
      },
      "engines": {
        "node": ">=18.0.0",
        "npm": ">=8.0.0"
      },
      "optionalDependencies": {
        "@rollup/rollup-android-arm-eabi": "4.61.1",
        "@rollup/rollup-android-arm64": "4.61.1",
        "@rollup/rollup-darwin-arm64": "4.61.1",
        "@rollup/rollup-darwin-x64": "4.61.1",
        "@rollup/rollup-freebsd-arm64": "4.61.1",
        "@rollup/rollup-freebsd-x64": "4.61.1",
        "@rollup/rollup-linux-arm-gnueabihf": "4.61.1",
        "@rollup/rollup-linux-arm-musleabihf": "4.61.1",
        "@rollup/rollup-linux-arm64-gnu": "4.61.1",
        "@rollup/rollup-linux-arm64-musl": "4.61.1",
        "@rollup/rollup-linux-loong64-gnu": "4.61.1",
        "@rollup/rollup-linux-loong64-musl": "4.61.1",
        "@rollup/rollup-linux-ppc64-gnu": "4.61.1",
        "@rollup/rollup-linux-ppc64-musl": "4.61.1",
        "@rollup/rollup-linux-riscv64-gnu": "4.61.1",
        "@rollup/rollup-linux-riscv64-musl": "4.61.1",
        "@rollup/rollup-linux-s390x-gnu": "4.61.1",
        "@rollup/rollup-linux-x64-gnu": "4.61.1",
        "@rollup/rollup-linux-x64-musl": "4.61.1",
        "@rollup/rollup-openbsd-x64": "4.61.1",
        "@rollup/rollup-openharmony-arm64": "4.61.1",
        "@rollup/rollup-win32-arm64-msvc": "4.61.1",
        "@rollup/rollup-win32-ia32-msvc": "4.61.1",
        "@rollup/rollup-win32-x64-gnu": "4.61.1",
        "@rollup/rollup-win32-x64-msvc": "4.61.1",
        "fsevents": "~2.3.2"
      }
    },
    "node_modules/source-map-js": {
      "version": "1.2.1",
      "resolved": "https://registry.npmjs.org/source-map-js/-/source-map-js-1.2.1.tgz",
      "integrity": "sha512-UXWMKhLOwVKb728IUtQPXxfYU+usdybtUrK/8uGE8CQMvrhOpwvzDBwj0QhSL7MQc7vIsISBG8VQ8+IDQxpfQA==",
      "license": "BSD-3-Clause",
      "engines": {
        "node": ">=0.10.0"
      }
    },
    "node_modules/tinyglobby": {
      "version": "0.2.17",
      "resolved": "https://registry.npmjs.org/tinyglobby/-/tinyglobby-0.2.17.tgz",
      "integrity": "sha512-wXR/dYpcqKmfWpEdZjiKJOwCNFndD0DMnrW/cYjVGttEkBfVgcLFHoNrlj47mjOVic9yyNu65alsgF4NQyTa2g==",
      "dev": true,
      "license": "MIT",
      "dependencies": {
        "fdir": "^6.5.0",
        "picomatch": "^4.0.4"
      },
      "engines": {
        "node": ">=12.0.0"
      },
      "funding": {
        "url": "https://github.com/sponsors/SuperchupuDev"
      }
    },
    "node_modules/typescript": {
      "version": "5.8.3",
      "resolved": "https://registry.npmjs.org/typescript/-/typescript-5.8.3.tgz",
      "integrity": "sha512-p1diW6TqL9L07nNxvRMM7hMMw4c5XOo/1ibL4aAIGmSAt9slTE1Xgw5KWuof2uTOvCg9BY7ZRi+GaF+7sfgPeQ==",
      "devOptional": true,
      "license": "Apache-2.0",
      "bin": {
        "tsc": "bin/tsc",
        "tsserver": "bin/tsserver"
      },
      "engines": {
        "node": ">=14.17"
      }
    },
    "node_modules/vite": {
      "version": "6.4.3",
      "resolved": "https://registry.npmjs.org/vite/-/vite-6.4.3.tgz",
      "integrity": "sha512-NTKlcQjlAK7MlQoyb6LgaqHc8sso/pVyUJYWMws3jg21uTJw/LddqIFPcPqP6PzpgbIcZyKI85sFE4HBrQDA8A==",
      "dev": true,
      "license": "MIT",
      "dependencies": {
        "esbuild": "^0.25.0",
        "fdir": "^6.4.4",
        "picomatch": "^4.0.2",
        "postcss": "^8.5.3",
        "rollup": "^4.34.9",
        "tinyglobby": "^0.2.13"
      },
      "bin": {
        "vite": "bin/vite.js"
      },
      "engines": {
        "node": "^18.0.0 || ^20.0.0 || >=22.0.0"
      },
      "funding": {
        "url": "https://github.com/vitejs/vite?sponsor=1"
      },
      "optionalDependencies": {
        "fsevents": "~2.3.3"
      },
      "peerDependencies": {
        "@types/node": "^18.0.0 || ^20.0.0 || >=22.0.0",
        "jiti": ">=1.21.0",
        "less": "*",
        "lightningcss": "^1.21.0",
        "sass": "*",
        "sass-embedded": "*",
        "stylus": "*",
        "sugarss": "*",
        "terser": "^5.16.0",
        "tsx": "^4.8.1",
        "yaml": "^2.4.2"
      },
      "peerDependenciesMeta": {
        "@types/node": {
          "optional": true
        },
        "jiti": {
          "optional": true
        },
        "less": {
          "optional": true
        },
        "lightningcss": {
          "optional": true
        },
        "sass": {
          "optional": true
        },
        "sass-embedded": {
          "optional": true
        },
        "stylus": {
          "optional": true
        },
        "sugarss": {
          "optional": true
        },
        "terser": {
          "optional": true
        },
        "tsx": {
          "optional": true
        },
        "yaml": {
          "optional": true
        }
      }
    },
    "node_modules/vscode-uri": {
      "version": "3.1.0",
      "resolved": "https://registry.npmjs.org/vscode-uri/-/vscode-uri-3.1.0.tgz",
      "integrity": "sha512-/BpdSx+yCQGnCvecbyXdxHDkuk55/G3xwnC0GqY4gmQ3j+A+g8kzzgB4Nk/SINjqn6+waqw3EgbVF2QKExkRxQ==",
      "dev": true,
      "license": "MIT"
    },
    "node_modules/vue": {
      "version": "3.5.35",
      "resolved": "https://registry.npmjs.org/vue/-/vue-3.5.35.tgz",
      "integrity": "sha512-cx89fnr+0kVGHiNFG6y6s0bdjypJRFNZn6x3WPstNdQR1bi1mbB7h4v5IBGTsPJU3nK1+0Iqj3Zf+hZWMieR4Q==",
      "license": "MIT",
      "dependencies": {
        "@vue/compiler-dom": "3.5.35",
        "@vue/compiler-sfc": "3.5.35",
        "@vue/runtime-dom": "3.5.35",
        "@vue/server-renderer": "3.5.35",
        "@vue/shared": "3.5.35"
      },
      "peerDependencies": {
        "typescript": "*"
      },
      "peerDependenciesMeta": {
        "typescript": {
          "optional": true
        }
      }
    },
    "node_modules/vue-i18n": {
      "version": "10.0.8",
      "resolved": "https://registry.npmjs.org/vue-i18n/-/vue-i18n-10.0.8.tgz",
      "integrity": "sha512-mIjy4utxMz9lMMo6G9vYePv7gUFt4ztOMhY9/4czDJxZ26xPeJ49MAGa9wBAE3XuXbYCrtVPmPxNjej7JJJkZQ==",
      "deprecated": "v9 and v10 no longer supported. please migrate to v11. about maintenance status, see https://vue-i18n.intlify.dev/guide/maintenance.html",
      "license": "MIT",
      "dependencies": {
        "@intlify/core-base": "10.0.8",
        "@intlify/shared": "10.0.8",
        "@vue/devtools-api": "^6.5.0"
      },
      "engines": {
        "node": ">= 16"
      },
      "funding": {
        "url": "https://github.com/sponsors/kazupon"
      },
      "peerDependencies": {
        "vue": "^3.0.0"
      }
    },
    "node_modules/vue-router": {
      "version": "4.6.4",
      "resolved": "https://registry.npmjs.org/vue-router/-/vue-router-4.6.4.tgz",
      "integrity": "sha512-Hz9q5sa33Yhduglwz6g9skT8OBPii+4bFn88w6J+J4MfEo4KRRpmiNG/hHHkdbRFlLBOqxN8y8gf2Fb0MTUgVg==",
      "license": "MIT",
      "dependencies": {
        "@vue/devtools-api": "^6.6.4"
      },
      "funding": {
        "url": "https://github.com/sponsors/posva"
      },
      "peerDependencies": {
        "vue": "^3.5.0"
      }
    },
    "node_modules/vue-tsc": {
      "version": "2.2.12",
      "resolved": "https://registry.npmjs.org/vue-tsc/-/vue-tsc-2.2.12.tgz",
      "integrity": "sha512-P7OP77b2h/Pmk+lZdJ0YWs+5tJ6J2+uOQPo7tlBnY44QqQSPYvS0qVT4wqDJgwrZaLe47etJLLQRFia71GYITw==",
      "dev": true,
      "license": "MIT",
      "dependencies": {
        "@volar/typescript": "2.4.15",
        "@vue/language-core": "2.2.12"
      },
      "bin": {
        "vue-tsc": "bin/vue-tsc.js"
      },
      "peerDependencies": {
        "typescript": ">=5.0.0"
      }
    },
    "node_modules/zod": {
      "version": "3.25.76",
      "resolved": "https://registry.npmjs.org/zod/-/zod-3.25.76.tgz",
      "integrity": "sha512-gzUt/qt81nXsFGKIFcC3YnfEAx5NkunCfnDlvuBSSFS02bcXu4Lmea0AFIUwbLWxWPx3d9p8S5QoaujKcNQxcQ==",
      "license": "MIT",
      "funding": {
        "url": "https://github.com/sponsors/colinhacks"
      }
    }
  }
}

```

`package.json`:

```json
{
  "name": "resumainer-frontend",
  "version": "0.1.0",
  "private": true,
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vue-tsc --noEmit && vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "@primeuix/themes": "^1.0.2",
    "@primevue/forms": "^4.3.3",
    "primeicons": "^7.0.0",
    "primevue": "^4.3.3",
    "vue": "^3.5.13",
    "vue-i18n": "^10.0.7",
    "vue-router": "^4.5.0",
    "zod": "^3.24.3"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.2.3",
    "typescript": "~5.8.3",
    "vite": "^6.3.5",
    "vue-tsc": "^2.2.8"
  }
}

```

`src\App.vue`:

```vue
<template>
  <router-view v-slot="{ Component, route }">
    <transition name="page-fade" mode="out-in">
      <component :is="Component" :key="route.path" />
    </transition>
  </router-view>
  <Toast />
  <ConfirmDialog />
</template>

<script setup lang="ts">
import Toast from 'primevue/toast'
import ConfirmDialog from 'primevue/confirmdialog'
</script>

<style>
/* Page transition — subtle fade */
.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 200ms cubic-bezier(0.2, 0, 0, 1);
}

.page-fade-enter-from,
.page-fade-leave-to {
  opacity: 0;
}

/* Reduced motion */
@media (prefers-reduced-motion: reduce) {
  .page-fade-enter-active,
  .page-fade-leave-active {
    transition: none;
  }
}
</style>

```

`src\assets\styles\vue_general.css`:

```css
/* ============================================================
 * vue_general.css
 * ResumAIner Vue SPA — shared tokens, base styles, components.
 * Import order recommendation (in main.js / main.ts):
 *   1. PrimeVue base/theme CSS
 *   2. vue_general.css          ← this file
 *   3. primevue-overrides.css   ← if separate
 *   4. page-specific component styles (scoped)
 *
 * Import: import '@/assets/styles/vue_general.css'
 * ============================================================ */

/* ---- Design Tokens ---- */
:root {
  /* Canvas + Surfaces */
  --vue-bg-canvas: #F6F7FB;
  --vue-bg-surface: #FFFFFF;
  --vue-bg-subtle: #FBFCFE;
  --vue-bg-control: #F7FAFE;

  /* Text */
  --vue-text-primary: #10233F;
  --vue-text-secondary: #5D718B;
  --vue-text-muted: #8091A7;
  --vue-text-inverse: #FFFFFF;

  /* Borders */
  --vue-border-default: #D8DEE9;
  --vue-border-soft: #E3E8F0;
  --vue-border-subtle: #E8EDF4;
  --vue-border-control: #E5EAF2;

  /* Accent — Emerald */
  --vue-accent-primary: #0F9D7A;
  --vue-accent-primary-hover: #0C8467;
  --vue-accent-primary-active: #0A6F56;
  --vue-accent-bg-primary: #F2FFF9;

  /* Accent — Blue */
  --vue-accent-blue: #2F6BFF;
  --vue-accent-blue-hover: #1A54D9;
  --vue-accent-bg-blue: #EEF4FF;

  /* Accent — Violet (AI/system only) */
  --vue-accent-violet: #7C3AED;
  --vue-accent-bg-violet: #F5F3FF;

  /* Accent — Warning */
  --vue-accent-warning: #D97706;
  --vue-accent-bg-warning: #FFF7ED;

  /* Accent — Error */
  --vue-accent-error: #C2410C;
  --vue-accent-bg-error: #FFF5F0;
  --vue-accent-border-error: #FDDCC8;

  /* Accent — Success (same as primary) */
  --vue-accent-success: #0F9D7A;
  --vue-accent-bg-success: #F2FFF9;

  /* Typography */
  --vue-font-heading: 'Manrope', 'Inter', system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  --vue-font-body: 'Inter', system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  --vue-font-mono: 'JetBrains Mono', 'SF Mono', ui-monospace, Menlo, monospace;

  /* Font Sizes */
  --vue-text-xs: 12px;
  --vue-text-sm: 13px;
  --vue-text-base: 14px;
  --vue-text-md: 16px;
  --vue-text-lg: 18px;
  --vue-text-xl: 22px;
  --vue-text-2xl: 28px;
  --vue-text-3xl: 32px;

  /* Line Heights */
  --vue-leading-tight: 1.25;
  --vue-leading-body: 1.5;
  --vue-leading-relaxed: 1.6;

  /* Spacing (4px base) */
  --vue-space-1: 4px;
  --vue-space-2: 8px;
  --vue-space-3: 12px;
  --vue-space-4: 16px;
  --vue-space-5: 20px;
  --vue-space-6: 24px;
  --vue-space-8: 32px;
  --vue-space-10: 40px;
  --vue-space-12: 48px;
  --vue-space-16: 64px;

  /* Radius */
  --vue-radius-sm: 6px;
  --vue-radius-md: 8px;
  --vue-radius-lg: 12px;
  --vue-radius-xl: 16px;

  /* Shadows */
  --vue-shadow-card: 0 1px 3px rgba(16, 35, 63, 0.06), 0 1px 2px rgba(16, 35, 63, 0.04);
  --vue-shadow-elevated: 0 4px 12px rgba(16, 35, 63, 0.08);
  --vue-shadow-modal: 0 20px 60px rgba(16, 35, 63, 0.14);
  --vue-shadow-focus: 0 0 0 3px rgba(47, 107, 255, 0.2);
  --vue-shadow-error: 0 0 0 3px rgba(194, 65, 12, 0.10);

  /* Motion */
  --vue-motion-fast: 150ms;
  --vue-motion-base: 240ms;
  --vue-motion-slow: 420ms;
  --vue-motion-panel: 900ms;
  --vue-ease-standard: cubic-bezier(0.2, 0, 0, 1);
  --vue-ease-premium: cubic-bezier(0.16, 1, 0.3, 1);
}

/* ---- Reset & Base ---- */
*, *::before, *::after { box-sizing: border-box; }

html {
  -webkit-text-size-adjust: 100%;
  scroll-behavior: smooth;
}

body {
  margin: 0;
  background: var(--vue-bg-canvas);
  color: var(--vue-text-primary);
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-base);
  line-height: var(--vue-leading-body);
  text-rendering: optimizeLegibility;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

img, svg { display: block; max-width: 100%; }
a { color: var(--vue-accent-blue); text-decoration: none; }
a:hover { color: var(--vue-accent-blue-hover); text-decoration: underline; }
button { font: inherit; cursor: pointer; }
p { margin: 0; text-wrap: pretty; }
h1, h2, h3, h4, h5, h6 { margin: 0; text-wrap: balance; }

/* ---- Typography Utilities ---- */
.vue-h1 { font-family: var(--vue-font-heading); font-size: var(--vue-text-3xl); font-weight: 700; line-height: var(--vue-leading-tight); letter-spacing: -0.02em; }
.vue-h2 { font-family: var(--vue-font-heading); font-size: var(--vue-text-2xl); font-weight: 700; line-height: var(--vue-leading-tight); letter-spacing: -0.015em; }
.vue-h3 { font-family: var(--vue-font-heading); font-size: var(--vue-text-xl); font-weight: 600; line-height: var(--vue-leading-tight); letter-spacing: -0.01em; }
.vue-h4 { font-family: var(--vue-font-heading); font-size: var(--vue-text-md); font-weight: 600; line-height: var(--vue-leading-tight); }

.vue-body { font-family: var(--vue-font-body); font-size: var(--vue-text-base); line-height: var(--vue-leading-body); color: var(--vue-text-primary); }
.vue-body-sm { font-family: var(--vue-font-body); font-size: var(--vue-text-sm); line-height: var(--vue-leading-body); color: var(--vue-text-secondary); }
.vue-meta { font-family: var(--vue-font-body); font-size: var(--vue-text-xs); line-height: var(--vue-leading-body); color: var(--vue-text-muted); }
.vue-mono { font-family: var(--vue-font-mono); font-variant-numeric: tabular-nums; }

.vue-label { font-family: var(--vue-font-body); font-size: var(--vue-text-base); font-weight: 500; color: var(--vue-text-primary); margin-bottom: 4px; display: block; }
.vue-label-sm { font-family: var(--vue-font-body); font-size: var(--vue-text-sm); font-weight: 500; color: var(--vue-text-secondary); margin-bottom: 4px; display: block; }

/* ---- Layout Helpers ---- */
.vue-page { max-width: 1200px; margin: 0 auto; padding: var(--vue-space-6) var(--vue-space-6); }

.vue-page-header {
  display: flex; align-items: center; justify-content: space-between;
  gap: var(--vue-space-4); margin-bottom: var(--vue-space-6);
  flex-wrap: wrap;
}

.vue-row { display: flex; align-items: center; gap: var(--vue-space-4); }
.vue-row-between { display: flex; align-items: center; justify-content: space-between; gap: var(--vue-space-4); }
.vue-col { display: flex; flex-direction: column; gap: var(--vue-space-4); }
.vue-col-gap { display: flex; flex-direction: column; }
.vue-col-gap > * + * { margin-top: var(--vue-space-4); }

.vue-grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: var(--vue-space-6); }
.vue-grid-3 { display: grid; grid-template-columns: repeat(3, 1fr); gap: var(--vue-space-6); }
.vue-grid-4 { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--vue-space-6); }

.vue-flex-center { display: flex; align-items: center; justify-content: center; }
.vue-flex-wrap { display: flex; flex-wrap: wrap; gap: var(--vue-space-3); }
.vue-gap-2 { gap: var(--vue-space-2); }
.vue-gap-3 { gap: var(--vue-space-3); }
.vue-gap-4 { gap: var(--vue-space-4); }
.vue-gap-6 { gap: var(--vue-space-6); }

.vue-mt-4 { margin-top: var(--vue-space-4); }
.vue-mt-6 { margin-top: var(--vue-space-6); }
.vue-mt-8 { margin-top: var(--vue-space-8); }
.vue-mb-4 { margin-bottom: var(--vue-space-4); }
.vue-mb-6 { margin-bottom: var(--vue-space-6); }

/* ---- Top Bar ---- */
.vue-topbar {
  height: 56px; background: var(--vue-bg-surface);
  border-bottom: 1px solid var(--vue-border-soft);
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 var(--vue-space-6); position: sticky; top: 0; z-index: 100;
}

/* ---- Cards ---- */
.vue-card {
  background: var(--vue-bg-surface);
  border: 1px solid var(--vue-border-soft);
  border-radius: var(--vue-radius-lg);
  box-shadow: var(--vue-shadow-card);
  padding: var(--vue-space-5) var(--vue-space-6);
}

.vue-card-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: var(--vue-space-4); gap: var(--vue-space-4);
}

.vue-card-elevated {
  box-shadow: var(--vue-shadow-elevated);
}

.vue-card-flat {
  background: transparent; border: none; box-shadow: none; padding: 0;
}

/* ---- Buttons ---- */
.vue-btn {
  display: inline-flex; align-items: center; justify-content: center; gap: 8px;
  padding: 0 20px; height: 38px;
  border-radius: var(--vue-radius-md);
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-base); font-weight: 600;
  border: 1px solid transparent;
  cursor: pointer;
  transition: background var(--vue-motion-fast) var(--vue-ease-standard),
              border-color var(--vue-motion-fast) var(--vue-ease-standard),
              box-shadow var(--vue-motion-fast) var(--vue-ease-standard);
  white-space: nowrap;
  user-select: none;
  text-decoration: none;
  line-height: 1;
}

.vue-btn:focus-visible {
  outline: none;
  box-shadow: var(--vue-shadow-focus);
}

.vue-btn:disabled {
  opacity: 0.5; cursor: not-allowed; pointer-events: none;
}

.vue-btn-primary {
  background: var(--vue-accent-primary); border-color: var(--vue-accent-primary);
  color: var(--vue-text-inverse);
}
.vue-btn-primary:hover { background: var(--vue-accent-primary-hover); border-color: var(--vue-accent-primary-hover); }
.vue-btn-primary:active { background: var(--vue-accent-primary-active); border-color: var(--vue-accent-primary-active); }

.vue-btn-secondary {
  background: var(--vue-bg-surface); border-color: var(--vue-border-default);
  color: var(--vue-text-primary);
}
.vue-btn-secondary:hover { background: var(--vue-bg-subtle); border-color: var(--vue-border-control); color: var(--vue-text-primary); }

.vue-btn-ghost {
  background: transparent; border-color: transparent;
  color: var(--vue-text-secondary);
}
.vue-btn-ghost:hover { background: var(--vue-bg-subtle); color: var(--vue-text-primary); }

.vue-btn-danger {
  background: var(--vue-accent-error); border-color: var(--vue-accent-error);
  color: var(--vue-text-inverse);
}
.vue-btn-danger:hover { background: #A8380A; border-color: #A8380A; }

.vue-btn-sm { height: 32px; padding: 0 14px; font-size: var(--vue-text-sm); }
.vue-btn-lg { height: 44px; padding: 0 24px; font-size: var(--vue-text-md); }

/* ---- Inputs ---- */
.vue-input {
  display: block; width: 100%;
  background: var(--vue-bg-surface);
  border: 1px solid var(--vue-border-control);
  border-radius: var(--vue-radius-md);
  padding: 10px 12px;
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-base);
  color: var(--vue-text-primary);
  line-height: 1.4;
  transition: border-color var(--vue-motion-fast) var(--vue-ease-standard),
              box-shadow var(--vue-motion-fast) var(--vue-ease-standard);
}

.vue-input::placeholder { color: var(--vue-text-muted); }

.vue-input:focus {
  border-color: var(--vue-accent-blue);
  box-shadow: var(--vue-shadow-focus);
  outline: none;
}

.vue-input.error,
.vue-input.vue-input-error {
  border-color: var(--vue-accent-error);
  box-shadow: var(--vue-shadow-error);
}

.vue-input:disabled {
  background: var(--vue-bg-subtle);
  color: var(--vue-text-muted);
  cursor: not-allowed;
}

textarea.vue-input {
  min-height: 100px;
  resize: vertical;
}

select.vue-input {
  appearance: auto;
}

/* ---- Form Groups ---- */
.vue-form-group {
  display: flex; flex-direction: column; gap: 4px;
}

.vue-form-label {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-base);
  font-weight: 500;
  color: var(--vue-text-primary);
}

.vue-form-label .vue-required {
  color: var(--vue-accent-error);
  margin-left: 2px;
}

.vue-form-hint {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-sm);
  color: var(--vue-text-muted);
}

.vue-form-error {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-sm);
  color: var(--vue-accent-error);
}

/* ---- Chips / Badges ---- */
.vue-chip {
  display: inline-flex; align-items: center;
  padding: 2px 10px; height: 24px;
  border-radius: 999px;
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-xs); font-weight: 500;
  border: 1px solid var(--vue-border-soft);
  background: var(--vue-bg-subtle);
  color: var(--vue-text-secondary);
  white-space: nowrap;
}

.vue-chip-primary {
  background: var(--vue-accent-bg-primary);
  border-color: var(--vue-accent-primary);
  color: var(--vue-accent-primary);
}

.vue-chip-success {
  background: var(--vue-accent-bg-success);
  border-color: var(--vue-accent-primary);
  color: var(--vue-accent-primary);
}

.vue-chip-warning {
  background: var(--vue-accent-bg-warning);
  border-color: var(--vue-accent-warning);
  color: var(--vue-accent-warning);
}

.vue-chip-error {
  background: var(--vue-accent-bg-error);
  border-color: var(--vue-accent-border-error);
  color: var(--vue-accent-error);
}

.vue-chip-role {
  background: var(--vue-accent-bg-violet);
  border-color: var(--vue-accent-violet);
  color: var(--vue-accent-violet);
}

/* ---- Alerts ---- */
.vue-alert {
  display: flex; align-items: flex-start; gap: 10px;
  padding: 12px 16px;
  border-radius: var(--vue-radius-md);
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-base);
  line-height: var(--vue-leading-body);
  border: 1px solid transparent;
}

.vue-alert-success {
  background: var(--vue-accent-bg-success);
  border-color: var(--vue-accent-primary);
  color: var(--vue-text-primary);
}

.vue-alert-error {
  background: var(--vue-accent-bg-error);
  border-color: var(--vue-accent-border-error);
  color: var(--vue-text-primary);
}

.vue-alert-warning {
  background: var(--vue-accent-bg-warning);
  border-color: var(--vue-accent-warning);
  color: var(--vue-text-primary);
}

.vue-alert-info {
  background: var(--vue-accent-bg-blue);
  border-color: var(--vue-accent-blue);
  color: var(--vue-text-primary);
}

/* ---- Language Switcher ---- */
.vue-lang-switch {
  display: inline-flex;
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-sm);
  font-weight: 600;
  border: 1px solid var(--vue-border-soft);
  border-radius: 6px;
  overflow: hidden;
}

.vue-lang-switch button,
.vue-lang-switch .vue-lang-btn {
  padding: 4px 10px; height: 30px;
  background: transparent; border: none;
  color: var(--vue-text-muted);
  cursor: pointer;
  font-weight: 600; font-size: var(--vue-text-sm);
  transition: background var(--vue-motion-fast) var(--vue-ease-standard),
              color var(--vue-motion-fast) var(--vue-ease-standard);
}

.vue-lang-switch button:hover,
.vue-lang-switch .vue-lang-btn:hover {
  color: var(--vue-text-primary);
}

.vue-lang-switch button.active,
.vue-lang-switch .vue-lang-btn.active {
  background: var(--vue-accent-blue);
  color: var(--vue-text-inverse);
}

/* ---- Empty States ---- */
.vue-empty {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  text-align: center;
  padding: var(--vue-space-12) var(--vue-space-6);
  gap: var(--vue-space-3);
}

.vue-empty-icon {
  width: 48px; height: 48px;
  color: var(--vue-text-muted);
  margin-bottom: var(--vue-space-2);
}

.vue-empty-title {
  font-family: var(--vue-font-heading);
  font-size: var(--vue-text-lg);
  font-weight: 600;
  color: var(--vue-text-primary);
}

.vue-empty-desc {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-base);
  color: var(--vue-text-secondary);
  max-width: 36ch;
  line-height: var(--vue-leading-body);
}

/* ---- Page Section Spacing ---- */
.vue-section { padding-block: var(--vue-space-8); }
.vue-section-sm { padding-block: var(--vue-space-6); }

/* ---- Divider ---- */
.vue-divider { border: none; border-top: 1px solid var(--vue-border-soft); margin: 0; }

/* ---- Auth gradient (reusable token reference) ---- */
/* The Auth Page uses this gradient for its animated panel.
   Other pages should NOT use it unless for a similarly premium accent.
   Usage: background: var(--vue-gradient-auth); */
:root {
  --vue-gradient-auth: linear-gradient(135deg, #0F9D7A 0%, #2F6BFF 100%);
  --vue-gradient-auth-soft: linear-gradient(135deg, rgba(15, 157, 122, 0.95), rgba(47, 107, 255, 0.88));
}

/* ---- Animation Utilities ---- */
.vue-fade-in {
  animation: vue-fade-in var(--vue-motion-base) var(--vue-ease-standard) both;
}

.vue-fade-in-up {
  animation: vue-fade-in-up var(--vue-motion-base) var(--vue-ease-standard) both;
}

@keyframes vue-fade-in {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes vue-fade-in-up {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ---- Responsive Helpers ---- */
.vue-hide-mobile { display: revert; }
.vue-show-mobile { display: none; }

@media (max-width: 639px) {
  .vue-hide-mobile { display: none; }
  .vue-show-mobile { display: revert; }
  .vue-grid-2, .vue-grid-3, .vue-grid-4 { grid-template-columns: 1fr; }
  .vue-page { padding: var(--vue-space-4) var(--vue-space-4); }
}

@media (min-width: 640px) and (max-width: 1023px) {
  .vue-grid-3, .vue-grid-4 { grid-template-columns: repeat(2, 1fr); }
  .vue-page { padding: var(--vue-space-5) var(--vue-space-5); }
}

/* ---- Reduced Motion ---- */
@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
    scroll-behavior: auto !important;
  }
}

/* ---- PrimeVue-friendly base overrides ---- */
/* These provide a consistent foundation for PrimeVue components.
   More specific overrides can go in primevue-overrides.css if needed. */

.p-inputtext,
.p-password-input {
  font-family: var(--vue-font-body) !important;
  font-size: var(--vue-text-base) !important;
  background: var(--vue-bg-surface) !important;
  border-color: var(--vue-border-control) !important;
  color: var(--vue-text-primary) !important;
  border-radius: var(--vue-radius-md) !important;
  padding: 10px 12px !important;
  transition: border-color var(--vue-motion-fast), box-shadow var(--vue-motion-fast) !important;
}

.p-inputtext:focus,
.p-password-input:focus {
  border-color: var(--vue-accent-blue) !important;
  box-shadow: var(--vue-shadow-focus) !important;
}

.p-button {
  font-family: var(--vue-font-body) !important;
  font-weight: 600 !important;
  border-radius: var(--vue-radius-md) !important;
}

.p-button.p-button-primary {
  background: var(--vue-accent-primary) !important;
  border-color: var(--vue-accent-primary) !important;
}

.p-button.p-button-primary:hover {
  background: var(--vue-accent-primary-hover) !important;
  border-color: var(--vue-accent-primary-hover) !important;
}

.p-datatable .p-datatable-thead > tr > th {
  background: var(--vue-bg-subtle) !important;
  color: var(--vue-text-secondary) !important;
  font-family: var(--vue-font-body) !important;
  font-size: var(--vue-text-sm) !important;
  font-weight: 600 !important;
  border-color: var(--vue-border-soft) !important;
}

.p-datatable .p-datatable-tbody > tr {
  border-color: var(--vue-border-soft) !important;
}

.p-datatable .p-datatable-tbody > tr:hover {
  background: var(--vue-bg-subtle) !important;
}

.p-dialog-header {
  border-bottom: 1px solid var(--vue-border-soft) !important;
  padding: var(--vue-space-5) var(--vue-space-6) !important;
}

.p-dialog-content {
  padding: var(--vue-space-5) var(--vue-space-6) !important;
}

.p-dialog-footer {
  border-top: 1px solid var(--vue-border-soft) !important;
  padding: var(--vue-space-4) var(--vue-space-6) !important;
}

.p-toast .p-toast-message {
  border-radius: var(--vue-radius-lg) !important;
}

.p-toast .p-toast-message.p-toast-message-success {
  background: var(--vue-accent-bg-success) !important;
  border-color: var(--vue-accent-primary) !important;
}

.p-toast .p-toast-message.p-toast-message-error {
  background: var(--vue-accent-bg-error) !important;
  border-color: var(--vue-accent-border-error) !important;
}

.p-toast .p-toast-message.p-toast-message-warn {
  background: var(--vue-accent-bg-warning) !important;
  border-color: var(--vue-accent-warning) !important;
}

.p-toast .p-toast-message.p-toast-message-info {
  background: var(--vue-accent-bg-blue) !important;
  border-color: var(--vue-accent-blue) !important;
}

/* ============================================================
 * PrimeVue Button Overrides — ResumAIner accent color
 * Matches prototype's --accent: #0F9D7A
 * ============================================================ */
.p-button.p-button-success {
  background: #0F9D7A !important;
  border-color: #0F9D7A !important;
  color: #fff !important;
}
.p-button.p-button-success:hover {
  background: #0C8467 !important;
  border-color: #0C8467 !important;
}
.p-button.p-button-success.p-button-outlined {
  background: transparent !important;
  border-color: #0F9D7A !important;
  color: #0F9D7A !important;
}
.p-button.p-button-success.p-button-outlined:hover {
  background: rgba(15,157,122,0.08) !important;
  border-color: #0C8467 !important;
  color: #0C8467 !important;
}
.p-button.p-button-danger {
  background: transparent !important;
  border-color: #C2410C !important;
  color: #C2410C !important;
}
.p-button.p-button-danger:hover {
  background: #C2410C !important;
  border-color: #C2410C !important;
  color: #fff !important;
}

```

`src\components\AppHeader.vue`:

```vue
<template>
  <header class="resumainer-nav">
    <div class="nav-logo" @click="$router.push('/home')">
      <svg width="32" height="32" viewBox="0 0 32 32" fill="none" shape-rendering="geometricPrecision">
        <rect x="5" y="3.5" width="22" height="25" rx="4" fill="#FFFDF8" stroke="#17211D" stroke-width="2.4"/>
        <circle cx="16" cy="12.2" r="5.8" fill="none" stroke="#17211D" stroke-width="1.9"/>
        <circle cx="16" cy="12.2" r="2.8" fill="#0F8A6A"/>
        <rect x="9.2" y="24" width="13.6" height="3" rx="0.9" fill="#0F8A6A"/>
      </svg>
      <span class="nav-brand">ResumAIner</span>
    </div>

    <!-- Desktop Navigation -->
    <nav class="nav-links-desktop">
      <router-link to="/home" active-class="router-link-exact-active">{{ $t('nav.home') }}</router-link>
      <router-link to="/profile/contact" active-class="router-link-exact-active">{{ $t('nav.myProfile') }}</router-link>
      <router-link to="/generate/vacancy" active-class="router-link-exact-active">{{ $t('nav.generateResume') }}</router-link>
      <router-link v-if="isAdmin" to="/admin" active-class="router-link-exact-active">{{ $t('nav.admin') }}</router-link>
    </nav>

    <!-- Right side -->
    <div class="nav-right">
      <!-- Hamburger first on mobile -->
      <button class="hamburger-btn" @click="mobileMenuOpen = true" :aria-label="$t('nav.mobileMenu')">
        <i class="pi pi-bars"></i>
      </button>
      <LanguageSwitcher />
      <button class="logout-btn" v-tooltip.top="$t('nav.logout')" :aria-label="$t('nav.logout')" @click="handleLogout" :disabled="loggingOut">
        <i class="pi pi-sign-out"></i>
      </button>
    </div>

    <!-- Mobile menu overlay -->
    <div v-if="mobileMenuOpen" class="mobile-menu-overlay" @click.self="mobileMenuOpen = false">
      <div class="mobile-menu-panel">
        <router-link to="/home" @click="mobileMenuOpen = false" active-class="router-link-exact-active">
          <i class="pi pi-home"></i> {{ $t('nav.home') }}
        </router-link>
        <router-link to="/profile/contact" @click="mobileMenuOpen = false" active-class="router-link-exact-active">
          <i class="pi pi-user"></i> {{ $t('nav.myProfile') }}
        </router-link>
        <router-link to="/generate/vacancy" @click="mobileMenuOpen = false" active-class="router-link-exact-active">
          <i class="pi pi-file-plus"></i> {{ $t('nav.generateResume') }}
        </router-link>
        <router-link v-if="isAdmin" to="/admin" @click="mobileMenuOpen = false" active-class="router-link-exact-active">
          <i class="pi pi-shield"></i> {{ $t('nav.admin') }}
        </router-link>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth'
import LanguageSwitcher from '@/components/LanguageSwitcher.vue'

const router = useRouter()
const { role, logout } = useAuth()
const loggingOut = ref(false)
const mobileMenuOpen = ref(false)

const isAdmin = computed(() => role.value === 'ADMIN')

async function handleLogout() {
  loggingOut.value = true
  try {
    await logout()
    router.push('/auth')
  } finally {
    loggingOut.value = false
  }
}
</script>

<style scoped>
.resumainer-nav {
  background: #fff;
  border-bottom: 1px solid #E2E6EE;
  padding: 0 32px;
  height: 68px;
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  gap: 36px;
}
.nav-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  height: 48px;
  flex-shrink: 0;
}
.nav-brand {
  font-family: 'Manrope', system-ui, sans-serif;
  font-size: 1.15rem;
  font-weight: 700;
  color: #10233F;
  letter-spacing: -0.3px;
}
.nav-links-desktop {
  display: flex;
  gap: 4px;
  flex: 1;
}
.nav-links-desktop a {
  padding: 8px 20px;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 500;
  color: #5D718B;
  text-decoration: none;
  transition: all 0.15s;
  white-space: nowrap;
}
.nav-links-desktop a:hover,
.nav-links-desktop a.router-link-exact-active {
  background: rgba(15,157,122,0.08);
  color: #0F9D7A;
}
.nav-right {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-left: auto;
}
.hamburger-btn {
  display: none;
  background: none;
  border: none;
  color: #5D718B;
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  font-size: 1.25rem;
  transition: all 0.15s;
}
.hamburger-btn:hover {
  background: rgba(15,157,122,0.08);
  color: #0F9D7A;
}
.logout-btn {
  background: none;
  border: none;
  color: #5D718B;
  cursor: pointer;
  padding: 8px 10px;
  border-radius: 8px;
  transition: all 0.15s;
  font-size: 1.1rem;
  display: flex;
  align-items: center;
}
.logout-btn:hover {
  background: rgba(15,157,122,0.08);
  color: #C2410C;
}
.mobile-menu-overlay {
  display: block;
  position: fixed;
  inset: 0;
  z-index: 200;
  background: rgba(0,0,0,0.3);
}
.mobile-menu-panel {
  background: #fff;
  width: 260px;
  height: 100%;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  box-shadow: 4px 0 20px rgba(0,0,0,0.1);
  animation: slideRight 0.2s ease-out;
}
@keyframes slideRight {
  from { transform: translateX(-20px); opacity: 0; }
  to { transform: translateX(0); opacity: 1; }
}
.mobile-menu-panel a {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 500;
  color: #10233F;
  text-decoration: none;
  transition: all 0.12s;
}
.mobile-menu-panel a:hover,
.mobile-menu-panel a.router-link-exact-active {
  background: rgba(15,157,122,0.08);
  color: #0F9D7A;
}
.mobile-menu-panel a .pi {
  font-size: 1.1rem;
  color: #8091A7;
}
@media (max-width: 640px) {
  .resumainer-nav {
    padding: 0 16px;
    gap: 8px;
    height: 60px;
  }
  .nav-links-desktop {
    display: none;
  }
  .hamburger-btn {
    display: flex;
    align-items: center;
  }
  .nav-right {
    gap: 6px;
  }
  .logout-btn {
    padding: 6px 8px;
  }
}
</style>

```

`src\components\LanguageSwitcher.vue`:

```vue
<template>
  <div class="vue-lang-switch" role="group" aria-label="Language switcher">
    <button
      :class="{ active: locale === 'en' }"
      :aria-pressed="locale === 'en'"
      @click="switchLang('en')"
    >
      EN
    </button>
    <button
      :class="{ active: locale === 'ru' }"
      :aria-pressed="locale === 'ru'"
      @click="switchLang('ru')"
    >
      RU
    </button>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const { locale } = useI18n()

function switchLang(lang: string) {
  locale.value = lang
  localStorage.setItem('locale', lang)
}
</script>

<style scoped>
.vue-lang-switch {
  display: inline-flex;
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-sm);
  font-weight: 600;
  border: 1px solid var(--vue-border-soft);
  border-radius: 6px;
  overflow: hidden;
}

.vue-lang-switch button {
  padding: 4px 10px;
  height: 30px;
  background: transparent;
  border: none;
  color: var(--vue-text-muted);
  cursor: pointer;
  font-weight: 600;
  font-size: var(--vue-text-sm);
  transition: background var(--vue-motion-fast) var(--vue-ease-standard),
              color var(--vue-motion-fast) var(--vue-ease-standard);
}

.vue-lang-switch button:hover {
  color: var(--vue-text-primary);
}

.vue-lang-switch button.active {
  background: var(--vue-accent-blue);
  color: var(--vue-text-inverse);
}
</style>

```

`src\components\common\GeneratePlaceholderPage.vue`:

```vue
<template>
  <div class="page">
    <AppHeader />
    <main class="main-content">
      <h1>Generate Resume</h1>
      <p>Placeholder - under construction</p>
    </main>
  </div>
</template>

<script setup lang="ts">
import AppHeader from '@/components/AppHeader.vue'
</script>

<style scoped>
.page { display: flex; flex-direction: column; min-height: 100vh; background: #F6F7FB; }
.main-content { max-width: 1280px; width: 100%; margin: 0 auto; padding: 36px 32px; }
</style>

```

`src\components\common\ProfilePlaceholderPage.vue`:

```vue
<template>
  <div class="page">
    <AppHeader />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'

const router = useRouter()

onMounted(() => {
  router.replace('/profile/contact')
})
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #F6F7FB;
}
</style>

```

`src\components\home\GuidedNextStep.vue`:

```vue
<template>
  <div>Guided Next Step (placeholder)</div>
</template>

<script setup lang="ts">
defineProps<{
  profileReady: boolean
  checklist: any
}>()
</script>

```

`src\components\home\ResumeDetailsDialog.vue`:

```vue
<template>
  <Dialog
    v-model:visible="visible"
    modal
    :header="$t('resumeDetails.title')"
    :style="{ maxWidth: '580px', width: '100%' }"
    :dismissableMask="true"
    :closable="true"
  >
    <template v-if="resume">
      <div class="detail-grid">
        <div class="detail-field">
          <div class="detail-label">{{ $t('home.table.resumeTitle') }}</div>
          <div class="detail-value">{{ resume.resumeTitle }}</div>
        </div>
        <div class="detail-field">
          <div class="detail-label">{{ $t('home.table.vacancy') }}</div>
          <div class="detail-value">{{ resume.vacancy }}</div>
        </div>
        <div class="detail-field">
          <div class="detail-label">{{ $t('home.table.company') }}</div>
          <div class="detail-value">{{ resume.company }}</div>
        </div>
        <div class="detail-field">
          <div class="detail-label">{{ $t('home.table.language') }}</div>
          <div class="detail-value">{{ resume.language === 'EN' ? $t('language.en') : $t('language.ru') }}</div>
        </div>
        <div class="detail-field">
          <div class="detail-label">{{ $t('home.table.adaptationLevel') }}</div>
          <div class="detail-value">{{ $t('adaptation.' + resume.adaptationLevel.toLowerCase()) }}</div>
        </div>
        <div class="detail-field">
          <div class="detail-label">{{ $t('home.table.created') }}</div>
          <div class="detail-value">{{ resume.createdAt }}</div>
        </div>
      </div>

      <div class="public-link-row">
        <i class="pi pi-link" style="color: #8091A7; font-size: 0.9rem;"></i>
        <span class="link-text">{{ resume.publicUrl }}</span>
        <Button :label="$t('resumeDetails.copyLink')" icon="pi pi-copy" class="p-button-text p-button-sm" @click="onCopyLink" />
      </div>

      <Accordion :activeIndex="coverLetterOpen ? 0 : undefined">
        <AccordionTab :header="$t('resumeDetails.coverLetter')">
          <template v-if="resume.coverLetter">
            <p class="cover-letter-text">{{ resume.coverLetter }}</p>
            <Button :label="$t('resumeDetails.copyCoverLetter')" icon="pi pi-copy" class="p-button-text p-button-sm" @click="onCopyCoverLetter" />
          </template>
          <p v-else style="color: #5D718B; font-size: 0.9rem;">{{ $t('resumeDetails.noCoverLetter') }}</p>
        </AccordionTab>
      </Accordion>

      <div class="modal-actions">
        <Button :label="$t('resumeDetails.view')" icon="pi pi-external-link" class="p-button-success" @click="viewResume" />
        <Button :label="$t('resumeDetails.downloadPdf')" icon="pi pi-download" class="p-button-success p-button-outlined" @click="downloadPdf" />
        <Button :label="$t('resumeDetails.delete')" icon="pi pi-trash" class="p-button-danger p-button-outlined" style="margin-left: auto;" @click="confirmDelete" />
      </div>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useConfirm } from 'primevue/useconfirm'
import { useToast } from 'primevue/usetoast'
import Dialog from 'primevue/dialog'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import Button from 'primevue/button'
import type { SavedResumeData } from '@/services/userHomeService'

const props = defineProps<{
  visible: boolean
  resume: SavedResumeData | null
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  delete: [id: number]
}>()

const { t } = useI18n()
const confirm = useConfirm()
const toast = useToast()

const coverLetterOpen = ref(false)
const visible = ref(props.visible)

async function onCopyLink() {
  if (!props.resume?.publicUrl) return
  try {
    await navigator.clipboard.writeText(props.resume.publicUrl)
    toast.add({ severity: 'success', summary: '', detail: t('resumeDetails.linkCopied'), life: 3000 })
  } catch {
    toast.add({ severity: 'error', summary: '', detail: 'Failed to copy', life: 3000 })
  }
}

async function onCopyCoverLetter() {
  if (!props.resume?.coverLetter) return
  try {
    await navigator.clipboard.writeText(props.resume.coverLetter)
    toast.add({ severity: 'success', summary: '', detail: t('resumeDetails.coverLetterCopied'), life: 3000 })
  } catch {
    toast.add({ severity: 'error', summary: '', detail: 'Failed to copy', life: 3000 })
  }
}

function viewResume() {
  if (props.resume?.pdfUrl) {
    window.open(props.resume.pdfUrl, '_blank')
  }
}

function downloadPdf() {
  if (props.resume?.pdfUrl) {
    const a = document.createElement('a')
    a.href = props.resume.pdfUrl
    a.download = `${props.resume.resumeTitle || 'resume'}.pdf`
    a.click()
  }
}

function confirmDelete() {
  confirm.require({
    header: t('deleteResume.title'),
    message: t('deleteResume.text'),
    rejectLabel: t('deleteResume.cancel'),
    acceptLabel: t('deleteResume.confirm'),
    accept: () => {
      if (props.resume) {
        emit('delete', props.resume.id)
      }
    }
  })
}
</script>

<style scoped>
.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
  margin-bottom: 1rem;
}
.detail-label {
  font-size: 0.75rem;
  font-weight: 600;
  color: #8091A7;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 0.15rem;
}
.detail-value {
  font-size: 0.9rem;
  color: #10233F;
}
.public-link-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem;
  background: #F9FAFB;
  border-radius: 8px;
  margin-bottom: 1rem;
}
.link-text {
  flex: 1;
  font-size: 0.85rem;
  color: #5D718B;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.cover-letter-text {
  font-size: 0.85rem;
  color: #374151;
  line-height: 1.5;
  white-space: pre-wrap;
  max-height: 200px;
  overflow-y: auto;
}
.modal-actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 1.25rem;
  padding-top: 1rem;
  border-top: 1px solid #E5E7EB;
}
@media (max-width: 480px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>

```

`src\components\home\SavedResumesTable.vue`:

```vue
<template>
  <div>
    <div class="table-toolbar">
      <div class="search-field">
        <i class="pi pi-search"></i>
        <InputText
          v-model="searchText"
          :placeholder="$t('home.table.searchPlaceholder')"
          @keyup="onSearchInput"
          class="search-input"
        />
      </div>
      <div class="filter-group">
        <div class="filter-field">
          <span class="filter-label">{{ $t('home.table.filterLanguage') }}</span>
          <MultiSelect
            v-model="selectedLanguages"
            :options="langOptions"
            optionLabel="label"
            optionValue="value"
            :placeholder="$t('home.table.language')"
            :maxSelectedLabels="2"
            :showToggleAll="false"
            :selectedItemsLabel="langAllSelectedLabel"
            class="filter-select"
            @change="onFiltersChange"
          />
        </div>
        <div class="filter-field">
          <span class="filter-label">{{ $t('home.table.filterAdaptation') }}</span>
          <MultiSelect
            v-model="selectedAdaptations"
            :options="adaptOptions"
            optionLabel="label"
            optionValue="value"
            :placeholder="$t('home.table.adaptationLevel')"
            :maxSelectedLabels="2"
            :showToggleAll="false"
            :selectedItemsLabel="adaptAllSelectedLabel"
            class="filter-select"
            @change="onFiltersChange"
          />
        </div>
        <div class="filter-field">
          <span class="filter-label">{{ $t('home.table.filterDate') }}</span>
          <div class="date-range-group">
            <DatePicker
              v-model="dateFrom"
              :placeholder="$t('home.table.dateFrom')"
              dateFormat="yy-mm-dd"
              class="filter-date"
              @date-select="onFiltersChange"
              @clear="onFiltersChange"
              showClear
            />
            <span class="date-range-sep">–</span>
            <DatePicker
              v-model="dateTo"
              :placeholder="$t('home.table.dateTo')"
              dateFormat="yy-mm-dd"
              class="filter-date"
              @date-select="onFiltersChange"
              @clear="onFiltersChange"
              showClear
            />
          </div>
        </div>
        <Button
          v-if="showClear"
          :label="$t('home.table.clear')"
          icon="pi pi-filter-slash"
          class="p-button-success p-button-outlined"
          v-tooltip.top="$t('home.table.clearTooltip')"
          @click="onClear"
        />
      </div>
    </div>

    <DataTable
      ref="dt"
      :value="resumes"
      lazy
      paginator
      :first="first"
      :rows="size"
      :rowsPerPageOptions="[10, 20, 50]"
      dataKey="id"
      :totalRecords="totalRecords"
      :loading="loading"
      :removableSort="true"
      :sortField="sortField"
      :sortOrder="sortOrder"
      @page="onPage"
      @sort="onSort"
      @row-click="onRowClick"
      :currentPageReportTemplate="$t('home.table.pageReport')"
      paginatorTemplate="CurrentPageReport FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown"
      :responsiveLayout="isMobile ? 'scroll' : 'stack'"
      :emptyMessage="$t('home.table.noResultsTitle')"
    >
      <Column field="resumeTitle" :sortable="true">
        <template #header>
          <span class="p-column-title" v-tooltip.top="sortTooltip('resumeTitle')">{{ $t('home.table.resumeTitle') }}</span>
        </template>
      </Column>
      <Column field="vacancy" :sortable="true">
        <template #header>
          <span class="p-column-title" v-tooltip.top="sortTooltip('vacancy')">{{ $t('home.table.vacancy') }}</span>
        </template>
        <template #body="{ data }">
          <span v-tooltip.top="data.vacancy" class="truncate-cell" style="max-width:220px">{{ data.vacancy }}</span>
        </template>
      </Column>
      <Column field="company" :sortable="true">
        <template #header>
          <span class="p-column-title" v-tooltip.top="sortTooltip('company')">{{ $t('home.table.company') }}</span>
        </template>
        <template #body="{ data }">
          <span v-tooltip.top="data.company" class="truncate-cell" style="max-width:200px">{{ data.company }}</span>
        </template>
      </Column>
      <Column field="language" :sortable="true">
        <template #header>
          <span class="p-column-title" v-tooltip.top="sortTooltip('language')">{{ $t('home.table.language') }}</span>
        </template>
        <template #body="{ data }">
          <Tag :value="data.language === 'EN' ? 'EN' : 'RU'" :severity="data.language === 'EN' ? 'info' : 'success'" />
        </template>
      </Column>
      <Column field="adaptationLevel" :sortable="true">
        <template #header>
          <span class="p-column-title" v-tooltip.top="sortTooltip('adaptationLevel')">{{ $t('home.table.adaptationLevel') }}</span>
        </template>
        <template #body="{ data }">
          <Tag :value="$t('adaptation.' + data.adaptationLevel.toLowerCase())" :severity="getAdaptationSeverity(data.adaptationLevel)" />
        </template>
      </Column>
      <Column field="createdAt" :sortable="true">
        <template #header>
          <span class="p-column-title" v-tooltip.top="sortTooltip('createdAt')">{{ $t('home.table.created') }}</span>
        </template>
      </Column>
      <template #empty>
        <div class="empty-state">
          <div class="empty-icon"><i class="pi pi-file"></i></div>
          <h3>{{ $t('home.table.emptyTitle') }}</h3>
          <p>{{ $t('home.table.noResultsText') }}</p>
        </div>
      </template>
    </DataTable>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Tag from 'primevue/tag'
import IconField from 'primevue/iconfield'
import InputIcon from 'primevue/inputicon'
import InputText from 'primevue/inputtext'
import MultiSelect from 'primevue/multiselect'
import DatePicker from 'primevue/datepicker'
import Button from 'primevue/button'
import type { SavedResumeData } from '@/services/userHomeService'

const props = defineProps<{
  resumes: SavedResumeData[]
  totalRecords: number
  loading: boolean
  first: number
  sortField: string
  sortOrder: number
  size: number
}>()

const emit = defineEmits<{
  page: [event: any]
  sort: [event: any]
  filter: [filters: any]
  search: [query: string]
  openResume: [resume: SavedResumeData]
}>()

const { t, locale } = useI18n()
const dt = ref()

// Reactive options — update when locale changes
const languageOptions = [
  { value: 'EN', en: 'English', ru: 'Английский' },
  { value: 'RU', en: 'Russian', ru: 'Русский' }
]
const adaptationOptions = [
  { value: 'MINIMAL', en: 'Minimal', ru: 'Минимальная' },
  { value: 'BALANCED', en: 'Balanced', ru: 'Сбалансированная' },
  { value: 'MAXIMUM', en: 'Maximum', ru: 'Максимальная' }
]

const langOptions = computed(() =>
  languageOptions.map(o => ({ value: o.value, label: o[locale.value as keyof typeof o] }))
)
const adaptOptions = computed(() =>
  adaptationOptions.map(o => ({ value: o.value, label: o[locale.value as keyof typeof o] }))
)

const langAllSelectedLabel = computed(() => {
  if (selectedLanguages.value.length === 2) return locale.value === 'ru' ? 'Все языки' : 'All languages'
  return undefined
})
const adaptAllSelectedLabel = computed(() => {
  if (selectedAdaptations.value.length === 3) return locale.value === 'ru' ? 'Все уровни' : 'All levels'
  return undefined
})

const isMobile = ref(false)

function updateIsMobile() {
  isMobile.value = window.innerWidth < 640
}

onMounted(() => {
  updateIsMobile()
  window.addEventListener('resize', updateIsMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', updateIsMobile)
})

// --- Filter state ---
const searchText = ref('')
const selectedLanguages = ref(['EN', 'RU'])
const selectedAdaptations = ref(['MINIMAL', 'BALANCED', 'MAXIMUM'])
const dateFrom = ref<Date | null>(null)
const dateTo = ref<Date | null>(null)

// Track default date range for Clear
const defaultDateFrom = ref<Date | null>(null)
const defaultDateTo = ref<Date | null>(null)

// Compute whether filters are in default state
const showClear = computed(() => {
  if (searchText.value) return true
  if (selectedLanguages.value.length !== 2) return true
  if (selectedAdaptations.value.length !== 3) return true
  if (dateFrom.value !== null) return true
  if (dateTo.value !== null) return true
  return false
})

function formatDate(d: Date | null): string | null {
  if (!d) return null
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

let debounceTimer: ReturnType<typeof setTimeout> | null = null

function onSearchInput(e: KeyboardEvent) {
  const val = (e.target as HTMLInputElement).value || ''
  if (debounceTimer) clearTimeout(debounceTimer)
  if (val.length >= 3 || val.length === 0) {
    debounceTimer = setTimeout(() => {
      emit('search', val)
    }, 300)
  }
}

function onFiltersChange() {
  emit('filter', {
    language: selectedLanguages.value,
    adaptationLevel: selectedAdaptations.value,
    dateFrom: formatDate(dateFrom.value),
    dateTo: formatDate(dateTo.value)
  })
}

function onClear() {
  searchText.value = ''
  selectedLanguages.value = ['EN', 'RU']
  selectedAdaptations.value = ['MINIMAL', 'BALANCED', 'MAXIMUM']
  dateFrom.value = defaultDateFrom.value
  dateTo.value = defaultDateTo.value
  try { dt.value?.resetPage() } catch {}
  onFiltersChange()
}

function onPage(event: any) { emit('page', event) }
function onSort(event: any) { emit('sort', event) }
function onRowClick(event: any) { emit('openResume', event.data as SavedResumeData) }

function getAdaptationSeverity(level: string): 'success' | 'info' | 'warn' | undefined {
  switch (level) {
    case 'MAXIMUM': return 'success'
    case 'BALANCED': return 'info'
    case 'MINIMAL': return 'warn'
    default: return undefined
  }
}

function sortTooltip(field: string): string {
  const sf = props.sortField
  const so = props.sortOrder
  if (sf !== field) return t('home.table.sortNotSorted')
  if (so === 1) return t('home.table.sortAsc')
  if (so === -1) return t('home.table.sortDesc')
  return t('home.table.sortNotSorted')
}
</script>

<style scoped>
.table-toolbar {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  margin-bottom: 18px;
  flex-wrap: wrap;
}
.search-field {
  position: relative;
  flex: 1;
  min-width: 220px;
  max-width: 420px;
}
.search-field .pi {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: #8091A7;
  font-size: 0.9rem;
  z-index: 1;
  pointer-events: none;
}
.search-field .p-inputtext {
  padding-left: 40px !important;
  width: 100%;
}
.filter-group {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
  align-items: flex-end;
}
.filter-field {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.filter-label {
  font-size: 0.75rem;
  font-weight: 600;
  color: #8091A7;
  text-transform: uppercase;
  letter-spacing: 0.03em;
}
.search-input {
  min-width: 220px;
}
.filter-select {
  min-width: 160px;
}
.filter-date {
  min-width: 130px;
}
.date-range-group {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}
.date-range-sep {
  color: #8091A7;
  font-size: 0.9rem;
}
.truncate-cell {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: help;
}
.empty-state {
  text-align: center;
  padding: 2rem;
}
.empty-icon {
  font-size: 2.5rem;
  color: #8091A7;
  margin-bottom: 0.5rem;
}
.empty-state h3 {
  font-family: 'Manrope', sans-serif;
  font-size: 1.1rem;
  color: #10233F;
  margin: 0 0 0.25rem;
}
.empty-state p {
  margin: 0;
  color: #5D718B;
  font-size: 0.9rem;
}
@media (max-width: 640px) {
  .table-toolbar {
    flex-direction: column;
    align-items: stretch;
  }
  .search-field {
    max-width: none;
  }
  .filter-group {
    flex-direction: column;
    align-items: stretch;
  }
  .filter-field {
    width: 100%;
  }
  .date-range-group {
    flex-direction: column;
    gap: 6px;
  }
  .date-range-sep {
    display: none;
  }
  .filter-select, .filter-date {
    min-width: 100%;
    width: 100%;
  }
}
</style>

```

`src\components\home\SummaryCards.vue`:

```vue
<template>
  <div>Summary Cards (placeholder)</div>
</template>

<script setup lang="ts">
defineProps<{
  savedResumesCount: number
  profileReady: boolean
  lastResume: any
}>()
</script>

```

`src\components\profile\EmptyRecordsState.vue`:

```vue
<template>
  <div class="vue-alert vue-alert-warning empty-state-block">
    <i class="pi pi-info-circle"></i>
    <div>
      <strong>{{ title }}</strong>
      <p v-if="hint" class="empty-hint">{{ hint }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  hint?: string
}>()
</script>

<style scoped>
.empty-state-block {
  margin-bottom: 16px;
}
.empty-hint {
  margin: 4px 0 0 !important;
  font-size: var(--vue-text-sm);
  color: var(--vue-text-secondary);
}
</style>

```

`src\components\profile\InlineRecordForm.vue`:

```vue
<template>
  <div v-if="visible" class="inline-record-form" ref="formRef">
    <div class="form-header">
      <h4 class="form-title">{{ isEditing ? $t(editLabelKey) : $t(addLabelKey) }}</h4>
    </div>
    <div class="form-fields">
      <div v-for="field in fields" :key="field.key" class="form-field" :class="{ 'field-full': field.fullWidth }">
        <label :for="'field-' + field.key" class="form-label">
          {{ field.label }}
          <span v-if="field.required" class="required-mark">*</span>
        </label>
        <InputText
          v-if="field.type === 'text' || field.type === 'url'"
          :id="'field-' + field.key"
          v-model="field.value"
          :placeholder="field.placeholder"
          class="form-input"
        />
        <Textarea
          v-else-if="field.type === 'textarea'"
          :id="'field-' + field.key"
          v-model="field.value"
          :placeholder="field.placeholder"
          class="form-input"
          rows="3"
        />
        <DatePicker
          v-else-if="field.type === 'date'"
          :id="'field-' + field.key"
          v-model="field.value"
          :placeholder="field.placeholder"
          view="month"
          dateFormat="MM yy"
          class="form-input"
          :showIcon="true"
        />
        <Checkbox
          v-else-if="field.type === 'checkbox'"
          :id="'field-' + field.key"
          v-model="field.value"
          :binary="true"
          :inputId="'field-' + field.key"
        />
      </div>
    </div>
    <div class="form-actions">
      <Button :label="$t(saveLabelKey)" icon="pi pi-check" class="p-button-success" @click="$emit('save')" />
      <Button :label="$t(cancelLabelKey)" class="p-button-text" @click="$emit('cancel')" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Textarea from 'primevue/textarea'
import DatePicker from 'primevue/datepicker'
import Checkbox from 'primevue/checkbox'

export interface FormField {
  key: string
  type: 'text' | 'textarea' | 'url' | 'date' | 'checkbox'
  label: string
  value: any
  required?: boolean
  placeholder?: string
  fullWidth?: boolean
}

const props = defineProps<{
  visible: boolean
  fields: FormField[]
  isEditing: boolean
  addLabelKey: string
  editLabelKey: string
  saveLabelKey: string
  cancelLabelKey: string
}>()

defineEmits<{
  save: []
  cancel: []
}>()

const formRef = ref<HTMLElement | null>(null)

async function scrollToForm() {
  await nextTick()
  if (formRef.value) {
    formRef.value.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

defineExpose({ scrollToForm })
</script>

<style scoped>
.inline-record-form {
  background: var(--vue-bg-subtle);
  border: 1px solid var(--vue-border-default);
  border-radius: var(--vue-radius-md);
  padding: 18px 20px;
  margin-bottom: 16px;
}
.form-header {
  margin-bottom: 14px;
}
.form-title {
  font-family: var(--vue-font-heading);
  font-size: var(--vue-text-md);
  font-weight: 600;
  color: var(--vue-text-primary);
  margin: 0;
}
.form-fields {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.form-field {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.field-full {
  grid-column: 1 / -1;
}
.form-label {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-sm);
  font-weight: 500;
  color: var(--vue-text-primary);
}
.required-mark {
  color: var(--vue-accent-error);
  margin-left: 2px;
}
.form-input {
  width: 100%;
}
.form-actions {
  display: flex;
  gap: 8px;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid var(--vue-border-soft);
}
</style>

```

`src\components\profile\ProfileMobileTabs.vue`:

```vue
<template>
  <nav class="profile-mobile-tabs">
    <router-link
      v-for="section in sections"
      :key="section.key"
      :to="section.route"
      class="mobile-tab-item"
      :class="{ active: activeSection === section.key }"
    >
      <div class="mobile-tab-name">{{ section.label }}</div>
      <div class="mobile-tab-status" :class="'status-' + section.statusType">
        {{ section.statusText }}
      </div>
    </router-link>
  </nav>
</template>

<script setup lang="ts">
import type { SectionStatus } from '@/types/profile'

defineProps<{
  sections: SectionStatus[]
  activeSection: string
}>()
</script>

<style scoped>
.profile-mobile-tabs {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 6px;
  margin-bottom: 20px;
}
.mobile-tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 10px 6px;
  border-radius: var(--vue-radius-md);
  background: var(--vue-bg-surface);
  border: 1px solid var(--vue-border-soft);
  text-decoration: none;
  transition: all var(--vue-motion-fast) var(--vue-ease-standard);
  gap: 2px;
}
.mobile-tab-item:hover {
  background: var(--vue-bg-subtle);
}
.mobile-tab-item.active {
  background: var(--vue-accent-bg-primary);
  border-color: var(--vue-accent-primary);
}
.mobile-tab-name {
  font-family: var(--vue-font-body);
  font-size: 12px;
  font-weight: 600;
  color: var(--vue-text-primary);
  line-height: 1.2;
}
.mobile-tab-item.active .mobile-tab-name {
  color: var(--vue-accent-primary);
}
.mobile-tab-status {
  font-family: var(--vue-font-body);
  font-size: 10px;
  color: var(--vue-text-muted);
  line-height: 1.1;
}
.mobile-tab-status.status-completed {
  color: var(--vue-accent-primary);
}
.mobile-tab-status.status-incomplete {
  color: var(--vue-accent-warning);
}
</style>

```

`src\components\profile\ProfileSectionHeader.vue`:

```vue
<template>
  <div class="profile-section-header">
    <div class="section-header-top">
      <Button
        icon="pi pi-arrow-left"
        class="p-button-text p-button-sm back-btn"
        :label="$t('nav.home')"
        @click="$router.push('/home')"
      />
    </div>
    <h2 class="section-title section-title-area">{{ title }}</h2>
    <p v-if="purpose" class="section-purpose">{{ purpose }}</p>
  </div>
</template>

<script setup lang="ts">
import Button from 'primevue/button'

defineProps<{
  title: string
  purpose?: string
}>()
</script>

<style scoped>
.profile-section-header {
  margin-bottom: 24px;
}
.section-header-top {
  margin-bottom: 12px;
}
.back-btn {
  padding: 4px 8px !important;
  color: var(--vue-text-secondary) !important;
}
.section-title {
  font-family: var(--vue-font-heading);
  font-size: var(--vue-text-2xl);
  font-weight: 700;
  color: var(--vue-text-primary);
  margin: 0 0 6px;
  letter-spacing: -0.015em;
}
.section-purpose {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-sm);
  color: var(--vue-text-muted);
  line-height: var(--vue-leading-body);
  margin: 0;
  max-width: 640px;
}
</style>

```

`src\components\profile\ProfileShell.vue`:

```vue
<template>
  <div class="profile-shell">
    <div class="shell-layout">
      <ProfileSidebar
        v-if="!isMobile"
        :sections="sections"
        :activeSection="activeSection"
        class="shell-sidebar"
      />
      <ProfileMobileTabs
        v-if="isMobile"
        :sections="sections"
        :activeSection="activeSection"
      />
      <div class="shell-content">
        <slot />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import type { SectionStatus } from '@/types/profile'
import ProfileSidebar from './ProfileSidebar.vue'
import ProfileMobileTabs from './ProfileMobileTabs.vue'

defineProps<{
  sections: SectionStatus[]
  activeSection: string
}>()

const isMobile = ref(false)

function checkMobile() {
  isMobile.value = window.innerWidth < 768
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})
</script>

<style scoped>
.profile-shell {
  flex: 1;
  max-width: 1280px;
  width: 100%;
  margin: 0 auto;
  padding: 24px 32px;
}
.shell-layout {
  display: flex;
  gap: 32px;
  align-items: flex-start;
}
.shell-sidebar {
  position: sticky;
  top: 92px;
}
.shell-content {
  flex: 1;
  min-width: 0;
}
@media (max-width: 767px) {
  .profile-shell {
    padding: 16px;
  }
  .shell-layout {
    flex-direction: column;
    gap: 0;
  }
}
</style>

```

`src\components\profile\ProfileSidebar.vue`:

```vue
<template>
  <nav class="profile-sidebar">
    <div class="sidebar-title">{{ $t('profile.title') }}</div>
    <div class="sidebar-sections">
      <router-link
        v-for="section in sections"
        :key="section.key"
        :to="section.route"
        class="sidebar-item"
        :class="{ active: activeSection === section.key }"
      >
        <div class="sidebar-item-name">{{ section.label }}</div>
        <div class="sidebar-item-status" :class="'status-' + section.statusType">
          {{ section.statusText }}
        </div>
      </router-link>
    </div>
  </nav>
</template>

<script setup lang="ts">
import type { SectionStatus } from '@/types/profile'

defineProps<{
  sections: SectionStatus[]
  activeSection: string
}>()
</script>

<style scoped>
.profile-sidebar {
  width: 220px;
  flex-shrink: 0;
}
.sidebar-title {
  font-family: var(--vue-font-heading);
  font-size: var(--vue-text-md);
  font-weight: 700;
  color: var(--vue-text-primary);
  margin-bottom: 16px;
  letter-spacing: -0.01em;
}
.sidebar-sections {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.sidebar-item {
  display: block;
  padding: 10px 14px;
  border-radius: var(--vue-radius-md);
  text-decoration: none;
  transition: all var(--vue-motion-fast) var(--vue-ease-standard);
  border-left: 3px solid transparent;
}
.sidebar-item:hover {
  background: var(--vue-bg-subtle);
}
.sidebar-item.active {
  background: var(--vue-accent-bg-primary);
  border-left-color: var(--vue-accent-primary);
}
.sidebar-item-name {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-base);
  font-weight: 500;
  color: var(--vue-text-primary);
  margin-bottom: 2px;
}
.sidebar-item.active .sidebar-item-name {
  color: var(--vue-accent-primary);
}
.sidebar-item-status {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-xs);
  color: var(--vue-text-muted);
}
.sidebar-item-status.status-completed {
  color: var(--vue-accent-primary);
}
.sidebar-item-status.status-incomplete {
  color: var(--vue-accent-warning);
}
.sidebar-item-status.status-count {
  color: var(--vue-text-muted);
}
.sidebar-item-status.status-no-records {
  color: var(--vue-text-muted);
}
</style>

```

`src\components\profile\RecordCard.vue`:

```vue
<template>
  <div class="record-card">
    <div class="record-card-header">
      <div class="record-card-title-row">
        <h4 class="record-card-title">{{ title }}</h4>
        <div class="record-card-actions">
          <Button
            :label="$t(editLabelKey)"
            icon="pi pi-pencil"
            class="p-button-text p-button-sm"
            @click="$emit('edit')"
          />
          <Button
            :label="$t(deleteLabelKey)"
            icon="pi pi-trash"
            class="p-button-text p-button-sm p-button-danger"
            @click="$emit('delete')"
          />
        </div>
      </div>
      <div class="record-card-meta">
        <span>{{ metaLine }}</span>
        <span v-if="chipLabel" class="vue-chip vue-chip-primary" style="margin-left: 8px;">{{ chipLabel }}</span>
      </div>
      <p v-if="description" class="record-card-desc">{{ description }}</p>
      <a v-if="url" :href="url" target="_blank" class="record-card-url" @click.stop>{{ url }}</a>
    </div>
  </div>
</template>

<script setup lang="ts">
import Button from 'primevue/button'

defineProps<{
  title: string
  metaLine: string
  description?: string
  url?: string
  chipLabel?: string
  editLabelKey?: string
  deleteLabelKey?: string
}>()

defineEmits<{
  edit: []
  delete: []
}>()
</script>

<style scoped>
.record-card {
  background: var(--vue-bg-surface);
  border: 1px solid var(--vue-border-soft);
  border-radius: var(--vue-radius-md);
  padding: 14px 16px;
  margin-bottom: 10px;
}
.record-card-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.record-card-title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px;
}
.record-card-title {
  font-family: var(--vue-font-heading);
  font-size: var(--vue-text-base);
  font-weight: 600;
  color: var(--vue-text-primary);
  margin: 0;
  line-height: 1.3;
}
.record-card-actions {
  display: flex;
  gap: 2px;
  flex-shrink: 0;
}
.record-card-meta {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-sm);
  color: var(--vue-text-secondary);
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}
.record-card-desc {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-sm);
  color: var(--vue-text-secondary);
  margin: 6px 0 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.record-card-url {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-xs);
  color: var(--vue-accent-blue);
  margin-top: 4px;
  display: inline-block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}
</style>

```

`src\components\profile\UnsavedChangesDialog.vue`:

```vue
<template>
  <Dialog
      v-model:visible="localVisible"
    modal
    :header="$t('profile.unsaved.title')"
    :style="{ maxWidth: '420px', width: '100%' }"
    :dismissableMask="true"
    :closable="true"
  >
    <p style="color: var(--vue-text-secondary); margin: 0;">{{ $t('profile.unsaved.message') }}</p>
    <template #footer>
      <div class="dialog-footer">
        <Button :label="$t('profile.unsaved.leave')" class="p-button-text" @click="onLeave" />
        <Button :label="$t('profile.unsaved.stay')" class="p-button-success" @click="onStay" autofocus />
      </div>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import Dialog from 'primevue/dialog'
import Button from 'primevue/button'

const props = defineProps<{
  visible: boolean
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  confirmLeave: []
  cancelStay: []
}>()

const localVisible = ref(props.visible)

watch(() => props.visible, (v) => {
  localVisible.value = v
})

watch(localVisible, (v) => {
  emit('update:visible', v)
})

function onLeave() {
  localVisible.value = false
  emit('confirmLeave')
}

function onStay() {
  localVisible.value = false
  emit('cancelStay')
}
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>

```

`src\components\profile\courses\CourseDialog.vue`:

```vue
<template>
  <Dialog
    v-model:visible="localVisible"
    modal
    :header="dialogTitle"
    :style="{ maxWidth: '560px', width: '100%' }"
    :dismissableMask="true"
    :closable="true"
    @hide="onClose"
  >
    <template v-if="mode === 'view' && course">
      <div class="detail-grid">
        <div class="detail-field">
          <div class="detail-label">{{ $t('profile.courses.courseName') }}</div>
          <div class="detail-value">{{ course.courseName }}</div>
        </div>
        <div class="detail-field">
          <div class="detail-label">{{ $t('profile.courses.provider') }}</div>
          <div class="detail-value">{{ course.provider }}</div>
        </div>
        <div class="detail-field">
          <div class="detail-label">{{ $t('profile.courses.startDate') }}</div>
          <div class="detail-value">{{ formatDate(course.startDate) }}</div>
        </div>
        <div class="detail-field">
          <div class="detail-label">{{ $t('profile.courses.endDate') }}</div>
          <div class="detail-value">{{ formatDate(course.endDate) }}</div>
        </div>
        <div class="detail-field" v-if="course.credentialUrl">
          <div class="detail-label">{{ $t('profile.courses.credentialUrl') }}</div>
          <div class="detail-value"><a :href="course.credentialUrl" target="_blank">{{ course.credentialUrl }}</a></div>
        </div>
        <div class="detail-field-full" v-if="course.skills">
          <div class="detail-label">{{ $t('profile.courses.skills') }}</div>
          <div class="detail-value">{{ course.skills }}</div>
        </div>
        <div class="detail-field-full" v-if="course.description">
          <div class="detail-label">{{ $t('profile.courses.description') }}</div>
          <div class="detail-value">{{ course.description }}</div>
        </div>
      </div>
      <div class="dialog-actions">
        <Button :label="$t('profile.courses.edit')" icon="pi pi-pencil" class="p-button-success p-button-outlined" @click="switchToEdit" />
        <Button :label="$t('profile.courses.delete')" icon="pi pi-trash" class="p-button-danger" style="margin-left: auto;" @click="confirmDelete" />
      </div>
    </template>

    <template v-else-if="mode !== 'view'">
      <div class="edit-form">
        <div class="form-group">
          <label class="form-label">{{ $t('profile.courses.courseName') }} <span class="required">*</span></label>
          <InputText v-model="editForm.courseName" class="form-input" />
          <small v-if="errors.courseName" class="field-error">{{ errors.courseName }}</small>
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('profile.courses.provider') }} <span class="required">*</span></label>
          <InputText v-model="editForm.provider" class="form-input" />
          <small v-if="errors.provider" class="field-error">{{ errors.provider }}</small>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">{{ $t('profile.courses.startDate') }} <span class="required">*</span></label>
            <DatePicker v-model="editForm.startDate" class="form-input" :showIcon="true" :maxDate="editForm.endDate || undefined" />
            <small v-if="errors.startDate" class="field-error">{{ errors.startDate }}</small>
          </div>
          <div class="form-group">
            <label class="form-label">{{ $t('profile.courses.endDate') }}</label>
            <DatePicker v-model="editForm.endDate" class="form-input" :showIcon="true" :minDate="editForm.startDate || undefined" />
            <small v-if="errors.endDate" class="field-error">{{ errors.endDate }}</small>
          </div>
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('profile.courses.credentialUrl') }}</label>
          <InputText v-model="editForm.credentialUrl" class="form-input" />
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('profile.courses.skills') }}</label>
          <InputText v-model="editForm.skills" class="form-input" />
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('profile.courses.description') }}</label>
          <Textarea v-model="editForm.description" class="form-input" rows="3" />
        </div>
      </div>
    </template>

    <template #footer>
      <div v-if="mode !== 'view'" class="dialog-actions">
        <p class="required-hint" style="margin:0 auto 0 0">{{ $t('profile.requiredHint') }}</p>
        <Button :label="$t('profile.courses.cancel')" class="p-button-text" @click="onClose" />
        <Button :label="$t('profile.courses.save')" icon="pi pi-check" class="p-button-success" @click="onSave" />
      </div>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useConfirm } from 'primevue/useconfirm'
import Dialog from 'primevue/dialog'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Textarea from 'primevue/textarea'
import DatePicker from 'primevue/datepicker'
import type { Course } from '@/types/profile'

const props = defineProps<{
  visible: boolean
  course: Course | null
  mode: 'view' | 'add' | 'edit'
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  save: [course: Course]
  delete: [id: string]
}>()

const { t, locale } = useI18n()
const confirm = useConfirm()

const localVisible = ref(props.visible)

watch(() => props.visible, (v) => { localVisible.value = v })
watch(localVisible, (v) => { emit('update:visible', v) })

const editForm = reactive({
  courseName: '',
  provider: '',
  startDate: null as Date | null,
  endDate: null as Date | null,
  credentialUrl: '',
  skills: '',
  description: ''
})

const errors = reactive({
  courseName: '',
  provider: '',
  startDate: '',
  endDate: ''
})

function resetEditForm() {
  editForm.courseName = ''
  editForm.provider = ''
  editForm.startDate = null
  editForm.endDate = null
  editForm.credentialUrl = ''
  editForm.skills = ''
  editForm.description = ''
  errors.courseName = ''
  errors.provider = ''
  errors.startDate = ''
  errors.endDate = ''
}

watch(() => props.course, (course) => {
  if (course && props.mode === 'edit') {
    editForm.courseName = course.courseName
    editForm.provider = course.provider
    editForm.startDate = course.startDate ? new Date(course.startDate) : null
    editForm.endDate = course.endDate ? new Date(course.endDate) : null
    editForm.credentialUrl = course.credentialUrl
    editForm.skills = course.skills
    editForm.description = course.description
  } else if (props.mode === 'add') {
    resetEditForm()
  }
})

const dialogTitle = computed(() => {
  if (props.mode === 'add') return t('profile.courses.addTitle')
  if (props.mode === 'edit') return t('profile.courses.editTitle')
  return t('profile.courses.detailsTitle')
})

function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const months = locale.value === 'ru'
    ? ['январь', 'февраль', 'март', 'апрель', 'май', 'июнь', 'июль', 'август', 'сентябрь', 'октябрь', 'ноябрь', 'декабрь']
    : ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December']
  return months[d.getMonth()] + ' ' + d.getFullYear()
}

function switchToEdit() {
  if (props.course) {
    editForm.courseName = props.course.courseName
    editForm.provider = props.course.provider
    editForm.startDate = props.course.startDate ? new Date(props.course.startDate) : null
    editForm.endDate = props.course.endDate ? new Date(props.course.endDate) : null
    editForm.credentialUrl = props.course.credentialUrl
    editForm.skills = props.course.skills
    editForm.description = props.course.description
    emit('update:visible', true)
  }
}

function validate(): boolean {
  let valid = true
  errors.courseName = ''
  errors.provider = ''
  errors.startDate = ''
  errors.endDate = ''

  if (!editForm.courseName) { errors.courseName = t('profile.contact.fieldRequired'); valid = false }
  if (!editForm.provider) { errors.provider = t('profile.contact.fieldRequired'); valid = false }
  if (!editForm.startDate) { errors.startDate = t('profile.contact.fieldRequired'); valid = false }

  if (editForm.startDate && editForm.endDate && editForm.endDate < editForm.startDate) {
    errors.endDate = t('profile.dateRangeError')
    valid = false
  }

  return valid
}

function onSave() {
  if (!validate()) return

  const course: Course = {
    id: props.course?.id || '',
    courseName: editForm.courseName,
    provider: editForm.provider,
    startDate: editForm.startDate ? editForm.startDate.toISOString() : '',
    endDate: editForm.endDate ? editForm.endDate.toISOString() : '',
    credentialUrl: editForm.credentialUrl,
    skills: editForm.skills,
    description: editForm.description
  }
  emit('save', course)
  localVisible.value = false
}

function onClose() {
  localVisible.value = false
}

function confirmDelete() {
  confirm.require({
    header: t('profile.deleteConfirm.title'),
    message: t('profile.deleteConfirm.message'),
    rejectLabel: t('profile.deleteConfirm.cancel'),
    acceptLabel: t('profile.deleteConfirm.confirm'),
    accept: () => {
      if (props.course) emit('delete', props.course.id)
      localVisible.value = false
    }
  })
}
</script>

<style scoped>
.required-hint {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-sm);
  color: var(--vue-text-muted);
}
.detail-grid {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.detail-field, .detail-field-full {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.detail-label {
  font-size: var(--vue-text-xs);
  font-weight: 600;
  color: var(--vue-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.03em;
}
.detail-value {
  font-size: var(--vue-text-base);
  color: var(--vue-text-primary);
}
.detail-value a {
  color: var(--vue-accent-blue);
  word-break: break-all;
}
.edit-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.form-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.form-label {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-sm);
  font-weight: 500;
  color: var(--vue-text-primary);
}
.required {
  color: var(--vue-accent-error);
  margin-left: 2px;
}
.form-input {
  width: 100%;
}
.field-error {
  color: var(--vue-accent-error);
  font-size: var(--vue-text-xs);
}
.required-hint {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-sm);
  color: var(--vue-text-muted);
}
.dialog-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
}
</style>

```

`src\components\profile\courses\CoursesTable.vue`:

```vue
<template>
  <div>
    <div class="table-toolbar">
      <div class="search-field">
        <i class="pi pi-search"></i>
        <InputText
          v-model="searchText"
          :placeholder="$t('profile.courses.searchPlaceholder')"
          @keyup="onSearchInput"
          class="search-input"
        />
      </div>
      <div class="filter-group">
        <div class="filter-field">
          <span class="filter-label">{{ $t('profile.courses.dateFrom') }}</span>
          <DatePicker v-model="dateFrom" class="filter-date" :maxDate="dateTo || undefined" @date-select="applyFilters" showClear />
        </div>
        <div class="filter-field">
          <span class="filter-label">{{ $t('profile.courses.dateTo') }}</span>
          <DatePicker v-model="dateTo" class="filter-date" :minDate="dateFrom || undefined" @date-select="applyFilters" showClear />
        </div>
      </div>
      <Button
        v-if="showClear"
        :label="$t('profile.courses.resetFilters')"
        icon="pi pi-filter-slash"
        class="p-button-success p-button-outlined"
        @click="onClear"
      />
      <Button :label="$t('profile.courses.add')" icon="pi pi-plus" class="p-button-success" @click="$emit('add')" style="margin-left: auto;" />
    </div>
    <div v-if="dateFilterError" class="filter-error">{{ dateFilterError }}</div>

    <DataTable
      ref="dtRef"
      :value="filteredCourses"
      :paginator="true"
      :rows="rowsPerPage"
      :rowsPerPageOptions="[10, 20, 50]"
      :sortField="sortField"
      :sortOrder="sortOrder"
      :removableSort="true"
      @sort="onSort"
      @row-click="onRowClick"
      dataKey="id"
      :currentPageReportTemplate="$t('profile.courses.pageReport')"
      paginatorTemplate="CurrentPageReport FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown"
      :emptyMessage="$t('profile.courses.emptyTitle')"
      :responsiveLayout="isMobile ? 'scroll' : 'stack'"
      :globalFilterFields="['courseName', 'provider', 'skills']"
    >
      <Column field="courseName" :sortable="true">
        <template #header>
          <span class="p-column-title" v-tooltip.top="sortTooltip('courseName')">{{ $t('profile.courses.courseColumn') }}</span>
        </template>
      </Column>
      <Column field="provider" :sortable="true">
        <template #header>
          <span class="p-column-title" v-tooltip.top="sortTooltip('provider')">{{ $t('profile.courses.providerColumn') }}</span>
        </template>
      </Column>
      <Column field="startDate" :sortable="true">
        <template #header>
          <span class="p-column-title" v-tooltip.top="sortTooltip('startDate')">{{ $t('profile.courses.startDate') }}</span>
        </template>
        <template #body="{ data }">
          {{ formatDate(data.startDate) }}
        </template>
      </Column>
      <Column field="endDate" :sortable="true">
        <template #header>
          <span class="p-column-title" v-tooltip.top="sortTooltip('endDate')">{{ $t('profile.courses.endDate') }}</span>
        </template>
        <template #body="{ data }">
          {{ formatDate(data.endDate) }}
        </template>
      </Column>
      <Column field="skills" :sortable="true">
        <template #header>
          <span class="p-column-title" v-tooltip.top="sortTooltip('skills')">{{ $t('profile.courses.skillsColumn') }}</span>
        </template>
        <template #body="{ data }">
          <span v-tooltip.top="data.skills" class="skills-cell">{{ truncateSkills(data.skills) }}</span>
        </template>
      </Column>
      <template #empty>
        <div class="empty-state">
          <div class="empty-icon"><i class="pi pi-book"></i></div>
          <h3>{{ $t('profile.courses.emptyTitle') }}</h3>
          <p>{{ $t('profile.courses.emptyHint') }}</p>
        </div>
      </template>
    </DataTable>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToast } from 'primevue/usetoast'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import InputText from 'primevue/inputtext'
import DatePicker from 'primevue/datepicker'
import Button from 'primevue/button'
import type { Course } from '@/types/profile'

const props = defineProps<{
  courses: Course[]
}>()

const emit = defineEmits<{
  add: []
  open: [course: Course]
}>()

const { t, locale } = useI18n()
const toast = useToast()

const searchText = ref('')
const dateFrom = ref<Date | null>(null)
const dateTo = ref<Date | null>(null)
const rowsPerPage = ref(10)
const sortField = ref('')
const sortOrder = ref(-1)
const isMobile = ref(false)
const dateFilterError = ref('')
const dtRef = ref()

function updateIsMobile() {
  isMobile.value = window.innerWidth < 640
}

onMounted(() => {
  updateIsMobile()
  window.addEventListener('resize', updateIsMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', updateIsMobile)
})

function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const months = locale.value === 'ru'
    ? ['январь', 'февраль', 'март', 'апрель', 'май', 'июнь', 'июль', 'август', 'сентябрь', 'октябрь', 'ноябрь', 'декабрь']
    : ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December']
  return months[d.getMonth()] + ' ' + d.getFullYear()
}

function truncateSkills(skills: string): string {
  if (!skills) return ''
  return skills.length > 40 ? skills.substring(0, 37) + '...' : skills
}

const filteredCourses = computed(() => {
  let result = [...props.courses]

  if (searchText.value && searchText.value.length >= 3) {
    const q = searchText.value.toLowerCase()
    result = result.filter(c =>
      c.courseName.toLowerCase().includes(q) ||
      c.provider.toLowerCase().includes(q) ||
      (c.skills && c.skills.toLowerCase().includes(q))
    )
  }

  if (dateFrom.value) {
    const from = dateFrom.value.getTime()
    result = result.filter(c => c.startDate ? new Date(c.startDate).getTime() >= from : false)
  }
  if (dateTo.value) {
    const to = dateTo.value.getTime()
    result = result.filter(c => c.startDate ? new Date(c.startDate).getTime() <= to : false)
  }

  return result
})

let debounceTimer: ReturnType<typeof setTimeout> | null = null

function onSearchInput(e: KeyboardEvent) {
  const val = (e.target as HTMLInputElement).value || ''
  if (debounceTimer) clearTimeout(debounceTimer)
  if (val.length >= 3 || val.length === 0) {
    debounceTimer = setTimeout(() => {
      searchText.value = val
    }, 300)
  }
}

function applyFilters() {
  if (dateFrom.value && dateTo.value && dateTo.value < dateFrom.value) {
    dateFilterError.value = t('profile.courses.dateFilterError')
    return
  }
  dateFilterError.value = ''
}

function onSort(event: any) {
  if (event.sortOrder === 0) {
    sortField.value = ''
    sortOrder.value = -1
  } else {
    sortField.value = event.sortField
    sortOrder.value = event.sortOrder
  }
}

function sortTooltip(field: string): string {
  if (sortField.value !== field) return t('home.table.sortNotSorted')
  if (sortOrder.value === 1) return t('home.table.sortAsc')
  if (sortOrder.value === -1) return t('home.table.sortDesc')
  return t('home.table.sortNotSorted')
}

const showClear = computed(() => {
  if (searchText.value) return true
  if (dateFrom.value !== null) return true
  if (dateTo.value !== null) return true
  if (sortField.value !== '') return true
  return false
})

function onClear() {
  searchText.value = ''
  dateFrom.value = null
  dateTo.value = null
  sortField.value = ''
  sortOrder.value = -1
  dateFilterError.value = ''
  rowsPerPage.value = 10
  try { dtRef.value?.resetPage() } catch {}
}

watch(dateFrom, () => {
  if (dateFrom.value && dateTo.value && dateTo.value < dateFrom.value) {
    dateTo.value = null
  }
  dateFilterError.value = ''
})

watch(dateTo, () => {
  if (dateTo.value && dateFrom.value && dateTo.value < dateFrom.value) {
    dateFilterError.value = t('profile.courses.dateFilterError')
  } else {
    dateFilterError.value = ''
  }
})

function onRowClick(event: any) {
  emit('open', event.data as Course)
}
</script>

<style scoped>
.table-toolbar {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  margin-bottom: 18px;
  flex-wrap: wrap;
}
.search-field {
  position: relative;
  flex: 1;
  min-width: 200px;
  max-width: 360px;
}
.search-field .pi {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: #8091A7;
  font-size: 0.9rem;
  z-index: 1;
  pointer-events: none;
}
.search-field .p-inputtext {
  padding-left: 40px !important;
  width: 100%;
}
.filter-group {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: flex-end;
}
.filter-field {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.filter-label {
  font-size: 0.75rem;
  font-weight: 600;
  color: #8091A7;
  text-transform: uppercase;
  letter-spacing: 0.03em;
}
.search-input {
  min-width: 200px;
}
.filter-date {
  min-width: 130px;
}
.filter-error {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-xs);
  color: var(--vue-accent-error);
  margin-bottom: 12px;
}
.skills-cell {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 200px;
  cursor: help;
}
.empty-state {
  text-align: center;
  padding: 2rem;
}
.empty-icon {
  font-size: 2.5rem;
  color: #8091A7;
  margin-bottom: 0.5rem;
}
.empty-state h3 {
  font-family: 'Manrope', sans-serif;
  font-size: 1.1rem;
  color: #10233F;
  margin: 0 0 0.25rem;
}
.empty-state p {
  margin: 0;
  color: #5D718B;
  font-size: 0.9rem;
}
@media (max-width: 640px) {
  .table-toolbar {
    flex-direction: column;
    align-items: stretch;
  }
  .search-field {
    max-width: none;
  }
  .filter-group {
    flex-direction: column;
    align-items: stretch;
  }
  .filter-date {
    min-width: 100%;
    width: 100%;
  }
}
</style>

```

`src\components\profile\sections\AdditionalInfoSection.vue`:

```vue
<template>
  <div>
    <ProfileSectionHeader
      :title="$t('profile.additional.title')"
    />

    <div class="additional-form">
      <!-- Block 1: Resume & Public Profile Preferences -->
      <div class="info-block">
        <h3 class="block-title">{{ $t('profile.additional.block1Title') }}</h3>
        <div class="block-fields">
          <div class="form-group">
            <label class="form-label">{{ $t('profile.additional.username') }} <span class="required">*</span></label>
            <InputText v-model="form.username" class="form-input" :placeholder="$t('profile.additional.usernamePlaceholder')" @blur="validateUsername" />
            <small v-if="formErrors.username" class="field-error">{{ formErrors.username }}</small>
            <p class="field-hint">{{ $t('profile.additional.usernameHelp') }}</p>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">{{ $t('profile.additional.defaultResumeLanguage') }} <span class="required">*</span></label>
              <Select v-model="form.defaultResumeLanguage" :options="languageOptions" optionLabel="label" optionValue="value" class="form-input" />
            </div>
            <div class="form-group">
              <label class="form-label">{{ $t('profile.additional.additionalResumeLanguage') }} <span class="required">*</span></label>
              <Select v-model="form.additionalResumeLanguage" :options="languageOptions" optionLabel="label" optionValue="value" class="form-input" />
            </div>
          </div>
        </div>
      </div>

      <!-- Block 2: Work Preferences -->
      <div class="info-block">
        <h3 class="block-title">{{ $t('profile.additional.block2Title') }}</h3>
        <div class="block-fields">
          <div class="form-group">
            <label class="form-label">{{ $t('profile.additional.acceptableWorkFormats') }}</label>
            <div class="checkbox-group">
              <div v-for="fmt in workFormatOptions" :key="fmt.value" class="checkbox-item">
                <Checkbox v-model="form.acceptableWorkFormats" :inputId="'wf-' + fmt.value" :value="fmt.value" :binary="false" />
                <label :for="'wf-' + fmt.value">{{ fmt.label }}</label>
              </div>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">{{ $t('profile.additional.willingnessToRelocate') }}</label>
              <Select v-model="form.willingnessToRelocate" :options="willingnessOptions" optionLabel="label" optionValue="value" class="form-input" />
            </div>
            <div class="form-group">
              <label class="form-label">{{ $t('profile.additional.willingnessForBusinessTravel') }}</label>
              <Select v-model="form.willingnessForBusinessTravel" :options="willingnessOptions" optionLabel="label" optionValue="value" class="form-input" />
            </div>
          </div>
        </div>
      </div>

      <!-- Block 3: Professional Info -->
      <div class="info-block">
        <h3 class="block-title">{{ $t('profile.additional.block3Title') }}</h3>
        <div class="block-fields">
          <div class="form-group">
            <label class="form-label">{{ $t('profile.additional.skills') }}</label>
            <Textarea v-model="form.skills" class="form-input" rows="3" :placeholder="$t('profile.additional.skillsPlaceholder')" />
          </div>
          <div class="form-group">
            <label class="form-label">{{ $t('profile.additional.spokenLanguages') }}</label>
            <InputText v-model="form.spokenLanguages" :placeholder="$t('profile.additional.spokenLanguagesPlaceholder')" class="form-input" />
          </div>
          <div class="form-group">
            <label class="form-label">{{ $t('profile.additional.professionalAspirations') }}</label>
            <Textarea v-model="form.professionalAspirations" class="form-input" rows="2" :placeholder="$t('profile.additional.professionalAspirationsPlaceholder')" />
          </div>
          <div class="form-group">
            <label class="form-label">{{ $t('profile.additional.achievements') }}</label>
            <Textarea v-model="form.achievements" class="form-input" rows="2" :placeholder="$t('profile.additional.achievementsPlaceholder')" />
          </div>
        </div>
      </div>

      <!-- Block 4: Personal Info -->
      <div class="info-block">
        <h3 class="block-title">{{ $t('profile.additional.block4Title') }}</h3>
        <div class="block-fields">
          <div class="form-group">
            <label class="form-label">{{ $t('profile.additional.additionalContextForAI') }}</label>
            <Textarea v-model="form.additionalContextForAI" class="form-input" rows="3" :placeholder="$t('profile.additional.additionalContextForAIPlaceholder')" />
            <p class="field-hint">{{ $t('profile.additional.additionalContextHint') }}</p>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">{{ $t('profile.additional.dateOfBirth') }}</label>
              <DatePicker v-model="form.dateOfBirth" class="form-input" :showIcon="true" />
            </div>
            <div class="form-group">
              <label class="form-label">{{ $t('profile.additional.citizenship') }}</label>
              <InputText v-model="form.citizenship" class="form-input" />
            </div>
          </div>
        </div>
      </div>

      <div class="form-actions">
        <p class="required-hint">{{ $t('profile.requiredHint') }}</p>
        <Button :label="$t('profile.additional.save')" icon="pi pi-check" class="p-button-success" :loading="saving" @click="handleSave" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useToast } from 'primevue/usetoast'
import { useI18n } from 'vue-i18n'
import InputText from 'primevue/inputtext'
import Textarea from 'primevue/textarea'
import DatePicker from 'primevue/datepicker'
import Select from 'primevue/select'
import Checkbox from 'primevue/checkbox'
import Button from 'primevue/button'
import { getAdditionalInfo, saveAdditionalInfo } from '@/services/profileMockService'
import type { AdditionalInfo } from '@/types/profile'
import ProfileSectionHeader from '../ProfileSectionHeader.vue'

const toast = useToast()
const { t, locale } = useI18n()
const saving = ref(false)

const emit = defineEmits<{
  saved: []
  dirtyChange: [dirty: boolean]
}>()

const languageOptions = computed(() => [
  { value: 'en', label: t('profile.additional.languageEnglish') },
  { value: 'ru', label: t('profile.additional.languageRussian') }
])

const workFormatOptions = computed(() => [
  { value: 'office', label: t('profile.additional.office') },
  { value: 'remote', label: t('profile.additional.remote') },
  { value: 'hybrid', label: t('profile.additional.hybrid') },
  { value: 'rotational', label: t('profile.additional.rotationalSchedule') }
])

const willingnessOptions = computed(() => [
  { value: 'yes', label: t('profile.additional.yes') },
  { value: 'no', label: t('profile.additional.no') },
  { value: 'negotiable', label: t('profile.additional.negotiable') }
])

const form = reactive<AdditionalInfo>({
  username: '',
  defaultResumeLanguage: '',
  additionalResumeLanguage: '',
  acceptableWorkFormats: [],
  willingnessToRelocate: '',
  willingnessForBusinessTravel: '',
  skills: '',
  spokenLanguages: '',
  professionalAspirations: '',
  achievements: '',
  additionalContextForAI: '',
  dateOfBirth: '',
  citizenship: ''
})

const formErrors = reactive<Record<string, string>>({
  username: ''
})

function validateUsername(): boolean {
  formErrors.username = ''
  const v = form.username
  if (!v) {
    formErrors.username = t('profile.contact.fieldRequired')
    return false
  }
  if (!/^[a-zA-Z0-9_-]+$/.test(v)) {
    formErrors.username = t('profile.additional.usernameInvalid')
    return false
  }
  return true
}

watch(() => form.defaultResumeLanguage, (newLang) => {
  if (newLang && newLang === form.additionalResumeLanguage) {
    form.additionalResumeLanguage = newLang === 'en' ? 'ru' : 'en'
  }
})

watch(() => form.additionalResumeLanguage, (newLang) => {
  if (newLang && newLang === form.defaultResumeLanguage) {
    form.defaultResumeLanguage = newLang === 'en' ? 'ru' : 'en'
  }
})

const original = ref<string>('')

function serialize(data: AdditionalInfo): string {
  return JSON.stringify(data)
}

function loadData() {
  const data = getAdditionalInfo()
  Object.assign(form, data)
  original.value = serialize({ ...data })
}

function isDirty(): boolean {
  return serialize({ ...form }) !== original.value
}

watch(form, () => {
  emit('dirtyChange', isDirty())
}, { deep: true })

async function handleSave() {
  if (!validateUsername()) return
  saving.value = true
  try {
    saveAdditionalInfo({ ...form })
    original.value = serialize({ ...form })
    emit('saved')
    emit('dirtyChange', false)
    toast.add({ severity: 'success', summary: '', detail: t('profile.saveSuccess'), life: 3000 })
  } catch {
    toast.add({ severity: 'error', summary: '', detail: t('profile.saveError'), life: 3000 })
  } finally {
    saving.value = false
  }
}

defineExpose({ isDirty, loadData })

onMounted(loadData)
</script>

<style scoped>
.required-hint {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-sm);
  color: var(--vue-text-muted);
  margin: 0 0 8px;
}
.additional-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.info-block {
  background: var(--vue-bg-surface);
  border: 1px solid var(--vue-border-soft);
  border-radius: var(--vue-radius-md);
  padding: 18px 20px;
}
.block-title {
  font-family: var(--vue-font-heading);
  font-size: var(--vue-text-md);
  font-weight: 600;
  color: var(--vue-text-primary);
  margin: 0 0 14px;
}
.block-fields {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.form-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.form-label {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-sm);
  font-weight: 500;
  color: var(--vue-text-primary);
}
.required {
  color: var(--vue-accent-error);
  margin-left: 2px;
}
.form-input {
  width: 100%;
}
.field-hint {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-xs);
  color: var(--vue-text-muted);
  margin: 4px 0 0;
  line-height: 1.4;
}
.checkbox-group {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 20px;
}
.checkbox-item {
  display: flex;
  align-items: center;
  gap: 6px;
}
.checkbox-item label {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-sm);
  color: var(--vue-text-primary);
  cursor: pointer;
}
.form-actions {
  margin-top: 4px;
  padding-top: 16px;
  border-top: 1px solid var(--vue-border-soft);
}
@media (max-width: 639px) {
  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>

```

`src\components\profile\sections\ContactDetailsSection.vue`:

```vue
<template>
  <div>
    <ProfileSectionHeader
      :title="$t('profile.contact.title')"
      :purpose="$t('profile.contact.purpose')"
    />

    <div class="contact-form">
      <div class="form-row">
        <div class="form-group">
          <label class="form-label">{{ $t('profile.contact.fullName') }} <span class="required">*</span></label>
          <InputText v-model="form.fullName" :placeholder="$t('profile.contact.fullNamePlaceholder')" class="form-input" @blur="validateField('fullName')" />
          <small v-if="errors.fullName" class="field-error">{{ errors.fullName }}</small>
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('profile.contact.professionalTitle') }} <span class="required">*</span></label>
          <InputText v-model="form.professionalTitle" :placeholder="$t('profile.contact.professionalTitlePlaceholder')" class="form-input" @blur="validateField('professionalTitle')" />
          <small v-if="errors.professionalTitle" class="field-error">{{ errors.professionalTitle }}</small>
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label class="form-label">{{ $t('profile.contact.email') }} <span class="required">*</span></label>
          <InputText v-model="form.email" :placeholder="$t('profile.contact.emailPlaceholder')" class="form-input" @blur="validateField('email')" />
          <small v-if="errors.email" class="field-error">{{ errors.email }}</small>
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('profile.contact.phone') }} <span class="required">*</span></label>
          <InputText v-model="form.phone" :placeholder="$t('profile.contact.phonePlaceholder')" class="form-input" @blur="validateField('phone')" />
          <small v-if="errors.phone" class="field-error">{{ errors.phone }}</small>
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label class="form-label">{{ $t('profile.contact.location') }} <span class="required">*</span></label>
          <InputText v-model="form.location" :placeholder="$t('profile.contact.locationPlaceholder')" class="form-input" @blur="validateField('location')" />
          <small v-if="errors.location" class="field-error">{{ errors.location }}</small>
        </div>
        <div class="form-group"></div>
      </div>
      <div class="form-divider"></div>
      <div class="form-row">
        <div class="form-group">
          <label class="form-label">{{ $t('profile.contact.linkedinUrl') }}</label>
          <InputText v-model="form.linkedinUrl" :placeholder="$t('profile.contact.linkedinPlaceholder')" class="form-input" @blur="validateField('linkedinUrl')" />
          <small v-if="errors.linkedinUrl" class="field-error">{{ errors.linkedinUrl }}</small>
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('profile.contact.portfolioUrl') }}</label>
          <InputText v-model="form.portfolioUrl" :placeholder="$t('profile.contact.portfolioPlaceholder')" class="form-input" @blur="validateField('portfolioUrl')" />
          <small v-if="errors.portfolioUrl" class="field-error">{{ errors.portfolioUrl }}</small>
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label class="form-label">{{ $t('profile.contact.telegram') }}</label>
          <InputText v-model="form.telegram" :placeholder="$t('profile.contact.telegramPlaceholder')" class="form-input" />
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('profile.contact.whatsapp') }}</label>
          <InputText v-model="form.whatsapp" :placeholder="$t('profile.contact.whatsappPlaceholder')" class="form-input" />
        </div>
      </div>

      <div class="form-actions">
        <p class="required-hint">{{ $t('profile.requiredHint') }}</p>
        <Button :label="$t('profile.contact.save')" icon="pi pi-check" class="p-button-success" :loading="saving" @click="handleSave" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useToast } from 'primevue/usetoast'
import { useI18n } from 'vue-i18n'
import InputText from 'primevue/inputtext'
import Button from 'primevue/button'
import { getContactDetails, saveContactDetails } from '@/services/profileMockService'
import type { ContactDetails } from '@/types/profile'
import ProfileSectionHeader from '../ProfileSectionHeader.vue'

const toast = useToast()
const { t } = useI18n()
const saving = ref(false)

const emit = defineEmits<{
  saved: []
  dirtyChange: [dirty: boolean]
}>()

const form = reactive<ContactDetails>({
  fullName: '',
  professionalTitle: '',
  email: '',
  phone: '',
  location: '',
  linkedinUrl: '',
  portfolioUrl: '',
  telegram: '',
  whatsapp: ''
})

const original = ref<string>('')

const errors = reactive<Record<string, string>>({
  fullName: '',
  professionalTitle: '',
  email: '',
  phone: '',
  location: '',
  linkedinUrl: '',
  portfolioUrl: ''
})

function isValidEmail(email: string): boolean {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
}

function isValidUrl(url: string): boolean {
  if (!url) return true
  return /^(https?:\/\/)?[\w\-]+(\.[\w\-]+)+[/#?]?.*$/.test(url)
}

function validateField(field: keyof typeof errors) {
  const v = form[field as keyof ContactDetails] as string
  errors[field] = ''
  if (field === 'email') {
    if (!v) errors.email = t('profile.contact.emailRequired')
    else if (!isValidEmail(v)) errors.email = t('profile.contact.emailInvalid')
  } else if (field === 'linkedinUrl') {
    if (v && !isValidUrl(v)) errors.linkedinUrl = t('profile.contact.urlInvalid')
  } else if (field === 'portfolioUrl') {
    if (v && !isValidUrl(v)) errors.portfolioUrl = t('profile.contact.urlInvalid')
  } else if (['fullName', 'professionalTitle', 'phone', 'location'].includes(field)) {
    if (!v) errors[field] = t('profile.contact.fieldRequired')
  }
}

function validateAll(): boolean {
  let valid = true
  const fields = ['fullName', 'professionalTitle', 'email', 'phone', 'location', 'linkedinUrl', 'portfolioUrl'] as const
  for (const f of fields) {
    validateField(f)
    if (errors[f]) valid = false
  }
  return valid
}

function serialize(data: ContactDetails): string {
  return JSON.stringify(data)
}

function loadData() {
  const data = getContactDetails()
  Object.assign(form, data)
  original.value = serialize({ ...data })
}

function isDirty(): boolean {
  return serialize({ ...form }) !== original.value
}

watch(form, () => {
  emit('dirtyChange', isDirty())
}, { deep: true })

async function handleSave() {
  if (!validateAll()) return
  saving.value = true
  try {
    saveContactDetails({ ...form })
    original.value = serialize({ ...form })
    emit('saved')
    emit('dirtyChange', false)
    toast.add({ severity: 'success', summary: '', detail: t('profile.saveSuccess'), life: 3000 })
  } catch {
    toast.add({ severity: 'error', summary: '', detail: t('profile.saveError'), life: 3000 })
  } finally {
    saving.value = false
  }
}

defineExpose({ isDirty, loadData })

onMounted(loadData)
</script>

<style scoped>
.required-hint {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-sm);
  color: var(--vue-text-muted);
  margin: 0 0 8px;
}
.contact-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.form-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.form-label {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-sm);
  font-weight: 500;
  color: var(--vue-text-primary);
}
.required {
  color: var(--vue-accent-error);
  margin-left: 2px;
}
.field-error {
  color: var(--vue-accent-error);
  font-size: var(--vue-text-xs);
}
.form-input {
  width: 100%;
}
.form-divider {
  border-top: 1px solid var(--vue-border-soft);
  margin: 4px 0;
}
.form-actions {
  margin-top: 12px;
  padding-top: 16px;
  border-top: 1px solid var(--vue-border-soft);
}
@media (max-width: 639px) {
  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>

```

`src\components\profile\sections\CoursesSection.vue`:

```vue
<template>
  <div>
    <ProfileSectionHeader
      :title="$t('profile.courses.title')"
      :purpose="$t('profile.courses.purpose')"
    />

    <CoursesTable
      :courses="records"
      @add="openAddDialog"
      @open="openViewDialog"
    />

    <CourseDialog
      v-model:visible="dialogVisible"
      :course="selectedCourse"
      :mode="dialogMode"
      @save="handleSave"
      @delete="handleDelete"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useToast } from 'primevue/usetoast'
import { useI18n } from 'vue-i18n'
import { getCourses, saveCourse, deleteCourse } from '@/services/profileMockService'
import type { Course } from '@/types/profile'
import ProfileSectionHeader from '../ProfileSectionHeader.vue'
import CoursesTable from '../courses/CoursesTable.vue'
import CourseDialog from '../courses/CourseDialog.vue'

const toast = useToast()
const { t } = useI18n()

const emit = defineEmits<{
  saved: []
  dirtyChange: [dirty: boolean]
}>()

const records = ref<Course[]>([])
const dialogVisible = ref(false)
const selectedCourse = ref<Course | null>(null)
const dialogMode = ref<'view' | 'add' | 'edit'>('view')

function loadRecords() {
  records.value = getCourses()
}

function openAddDialog() {
  selectedCourse.value = null
  dialogMode.value = 'add'
  dialogVisible.value = true
}

function openViewDialog(course: Course) {
  selectedCourse.value = course
  dialogMode.value = 'view'
  dialogVisible.value = true
}

function handleSave(course: Course) {
  try {
    records.value = saveCourse(course)
    emit('saved')
    emit('dirtyChange', false)
    toast.add({ severity: 'success', summary: '', detail: t('profile.saveSuccess'), life: 3000 })
  } catch {
    toast.add({ severity: 'error', summary: '', detail: t('profile.saveError'), life: 3000 })
  }
}

function handleDelete(id: string) {
  records.value = deleteCourse(id)
  emit('saved')
  emit('dirtyChange', false)
  toast.add({ severity: 'success', summary: '', detail: t('profile.deleteSuccess'), life: 3000 })
}

watch(dialogVisible, (visible) => {
  if (dialogMode.value !== 'view') {
    emit('dirtyChange', visible)
  }
})

watch(dialogMode, (mode) => {
  if (mode !== 'view' && dialogVisible.value) {
    emit('dirtyChange', true)
  }
})

defineExpose({ loadRecords })

onMounted(loadRecords)
</script>

```

`src\components\profile\sections\EducationSection.vue`:

```vue
<template>
  <div ref="sectionTopRef">
    <ProfileSectionHeader
      :title="$t('profile.education.title')"
      :purpose="$t('profile.education.purpose')"
    />

    <EmptyRecordsState
      v-if="records.length === 0 && !formVisible"
      :title="$t('profile.education.emptyTitle')"
      :hint="$t('profile.education.emptyHint')"
    />

    <Button
      v-if="!formVisible"
      :label="$t('profile.education.add')"
      icon="pi pi-plus"
      class="p-button-success p-button-outlined add-btn"
      @click="openAddForm"
    />

    <div v-if="formVisible" ref="formRef" class="inline-form">
      <div class="form-header">
        <h4 class="form-title">{{ editingId ? $t('profile.education.edit') : $t('profile.education.add') }}</h4>
      </div>
      <div class="form-fields">
        <div class="form-group">
          <label class="form-label">{{ $t('profile.education.institutionName') }} <span class="required">*</span></label>
          <InputText v-model="formData.institutionName" class="form-input" @blur="validateRequiredField('institutionName')" />
          <small v-if="formErrors.institutionName" class="field-error">{{ formErrors.institutionName }}</small>
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('profile.education.degree') }} <span class="required">*</span></label>
          <InputText v-model="formData.degree" class="form-input" @blur="validateRequiredField('degree')" />
          <small v-if="formErrors.degree" class="field-error">{{ formErrors.degree }}</small>
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('profile.education.fieldOfStudy') }}</label>
          <InputText v-model="formData.fieldOfStudy" class="form-input" />
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('profile.education.gpa') }}</label>
          <InputText v-model="formData.gpa" class="form-input" />
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('profile.education.startDate') }} <span class="required">*</span></label>
          <DatePicker v-model="formData.startDate" class="form-input" :showIcon="true" :maxDate="formData.endDate || undefined" />
          <small v-if="dateError" class="field-error">{{ dateError }}</small>
          <small v-if="formErrors.startDate" class="field-error">{{ formErrors.startDate }}</small>
        </div>
        <div class="form-group" v-if="!formData.currentlyStudying">
          <label class="form-label">{{ $t('profile.education.endDate') }}</label>
          <DatePicker v-model="formData.endDate" class="form-input" :showIcon="true" :minDate="formData.startDate || undefined" />
          <small v-if="dateError" class="field-error">{{ dateError }}</small>
        </div>
        <div class="form-group form-group-checkbox field-full">
          <Checkbox v-model="formData.currentlyStudying" :binary="true" inputId="edu-current" />
          <label for="edu-current">{{ $t('profile.education.currentlyStudying') }}</label>
        </div>
        <div class="form-group field-full">
          <label class="form-label">{{ $t('profile.education.location') }}</label>
          <InputText v-model="formData.location" class="form-input" />
        </div>
        <div class="form-group field-full">
          <label class="form-label">{{ $t('profile.education.comment') }}</label>
          <Textarea v-model="formData.comment" class="form-input" rows="2" />
        </div>
      </div>
      <div class="form-actions">
        <p class="required-hint">{{ $t('profile.requiredHint') }}</p>
        <Button :label="$t('profile.education.save')" icon="pi pi-check" class="p-button-success" @click="handleSave" />
        <Button :label="$t('profile.education.cancel')" class="p-button-text" @click="closeForm" />
      </div>
    </div>

    <RecordCard
      v-for="rec in records"
      :key="rec.id"
      :title="rec.institutionName"
      :metaLine="formatMeta(rec)"
      :description="formatDescription(rec)"
      :chipLabel="rec.currentlyStudying ? $t('profile.education.current') : undefined"
      editLabelKey="profile.education.edit"
      deleteLabelKey="profile.education.delete"
      @edit="openEditForm(rec)"
      @delete="confirmDelete(rec)"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick, watch } from 'vue'
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'
import { useI18n } from 'vue-i18n'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Textarea from 'primevue/textarea'
import DatePicker from 'primevue/datepicker'
import Checkbox from 'primevue/checkbox'
import { getEducation, saveEducationRecord, deleteEducationRecord } from '@/services/profileMockService'
import type { Education } from '@/types/profile'
import ProfileSectionHeader from '../ProfileSectionHeader.vue'
import RecordCard from '../RecordCard.vue'
import EmptyRecordsState from '../EmptyRecordsState.vue'

const toast = useToast()
const confirm = useConfirm()
const { t, locale } = useI18n()

const emit = defineEmits<{
  saved: []
  dirtyChange: [dirty: boolean]
}>()

const records = ref<Education[]>([])
const formVisible = ref(false)
const editingId = ref<string | null>(null)
const formRef = ref<HTMLElement | null>(null)
const sectionTopRef = ref<HTMLElement | null>(null)
const dateError = ref('')

const formData = reactive({
  institutionName: '',
  degree: '',
  fieldOfStudy: '',
  startDate: null as Date | null,
  endDate: null as Date | null,
  currentlyStudying: false,
  location: '',
  comment: '',
  gpa: ''
})

const formErrors = reactive<Record<string, string>>({
  institutionName: '',
  degree: '',
  startDate: ''
})

function validateRequiredField(field: string) {
  const val = formData[field as keyof typeof formData]
  formErrors[field as keyof typeof formErrors] = ''
  if (!val) {
    formErrors[field as keyof typeof formErrors] = t('profile.contact.fieldRequired')
  }
}

function validateRequired(): boolean {
  let valid = true
  const fields: (keyof typeof formErrors)[] = ['institutionName', 'degree', 'startDate']
  for (const f of fields) {
    const val = formData[f as keyof typeof formData] as string | Date | null
    formErrors[f] = val ? '' : t('profile.contact.fieldRequired')
    if (formErrors[f]) valid = false
  }
  return valid
}

function resetForm() {
  formData.institutionName = ''
  formData.degree = ''
  formData.fieldOfStudy = ''
  formData.startDate = null
  formData.endDate = null
  formData.currentlyStudying = false
  formData.location = ''
  formData.comment = ''
  formData.gpa = ''
  editingId.value = null
  dateError.value = ''
  Object.keys(formErrors).forEach(k => formErrors[k as keyof typeof formErrors] = '')
}

function loadRecords() {
  records.value = getEducation()
}

function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const months = locale.value === 'ru'
    ? ['январь', 'февраль', 'март', 'апрель', 'май', 'июнь', 'июль', 'август', 'сентябрь', 'октябрь', 'ноябрь', 'декабрь']
    : ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December']
  return months[d.getMonth()] + ' ' + d.getFullYear()
}

function formatMeta(rec: Education): string {
  const start = formatDate(rec.startDate)
  const end = rec.currentlyStudying ? t('profile.education.present') : formatDate(rec.endDate)
  const dateStr = start + ' — ' + end
  return (rec.location ? dateStr + ' · ' + rec.location : dateStr)
}

function formatDescription(rec: Education): string {
  const parts: string[] = []
  if (rec.degree) parts.push(rec.degree)
  if (rec.fieldOfStudy) parts.push(rec.fieldOfStudy)
  return parts.join('\n')
}

function scrollToSectionTitle() {
  nextTick(() => {
    sectionTopRef.value?.querySelector('.section-title-area')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  })
}

function openAddForm() {
  resetForm()
  formVisible.value = true
  scrollToSectionTitle()
}

function openEditForm(rec: Education) {
  formData.institutionName = rec.institutionName
  formData.degree = rec.degree
  formData.fieldOfStudy = rec.fieldOfStudy
  formData.startDate = rec.startDate ? new Date(rec.startDate) : null
  formData.endDate = rec.endDate ? new Date(rec.endDate) : null
  formData.currentlyStudying = rec.currentlyStudying
  formData.location = rec.location
  formData.comment = rec.comment
  formData.gpa = rec.gpa
  editingId.value = rec.id
  formVisible.value = true
  scrollToSectionTitle()
}

function closeForm() {
  formVisible.value = false
  resetForm()
  emit('dirtyChange', false)
}

function collectFormData(): Education {
  return {
    id: editingId.value || '',
    institutionName: formData.institutionName,
    degree: formData.degree,
    fieldOfStudy: formData.fieldOfStudy,
    startDate: formData.startDate ? formData.startDate.toISOString() : '',
    endDate: formData.currentlyStudying ? '' : (formData.endDate ? formData.endDate.toISOString() : ''),
    currentlyStudying: formData.currentlyStudying,
    location: formData.location,
    comment: formData.comment,
    gpa: formData.gpa
  }
}

function validateDates(): boolean {
  dateError.value = ''
  if (formData.startDate && formData.endDate && !formData.currentlyStudying) {
    if (formData.endDate < formData.startDate) {
      dateError.value = t('profile.dateRangeError')
      return false
    }
  }
  return true
}

function handleSave() {
  if (!validateDates()) return
  if (!validateRequired()) return
  try {
    records.value = saveEducationRecord(collectFormData())
    closeForm()
    emit('saved')
    emit('dirtyChange', false)
    toast.add({ severity: 'success', summary: '', detail: t('profile.saveSuccess'), life: 3000 })
  } catch {
    toast.add({ severity: 'error', summary: '', detail: t('profile.saveError'), life: 3000 })
  }
}

function confirmDelete(rec: Education) {
  confirm.require({
    header: t('profile.deleteConfirm.title'),
    message: t('profile.deleteConfirm.message'),
    rejectLabel: t('profile.deleteConfirm.cancel'),
    acceptLabel: t('profile.deleteConfirm.confirm'),
    accept: () => {
      records.value = deleteEducationRecord(rec.id)
      emit('saved')
      emit('dirtyChange', false)
      toast.add({ severity: 'success', summary: '', detail: t('profile.deleteSuccess'), life: 3000 })
    }
  })
}

watch(formVisible, (visible) => {
  emit('dirtyChange', visible)
})

defineExpose({ loadRecords })

onMounted(loadRecords)
</script>

<style scoped>
.required-hint {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-sm);
  color: var(--vue-text-muted);
  margin: 0 0 8px;
}
.add-btn {
  margin-bottom: 16px;
}
.inline-form {
  background: var(--vue-bg-subtle);
  border: 1px solid var(--vue-border-default);
  border-radius: var(--vue-radius-md);
  padding: 18px 20px;
  margin-bottom: 16px;
}
.form-header {
  margin-bottom: 14px;
}
.form-title {
  font-family: var(--vue-font-heading);
  font-size: var(--vue-text-md);
  font-weight: 600;
  color: var(--vue-text-primary);
  margin: 0;
}
.form-fields {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.form-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.form-group-checkbox {
  flex-direction: row;
  align-items: center;
  gap: 8px;
}
.form-group-checkbox label {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-sm);
  color: var(--vue-text-primary);
  cursor: pointer;
}
.field-full {
  grid-column: 1 / -1;
}
.form-label {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-sm);
  font-weight: 500;
  color: var(--vue-text-primary);
}
.required {
  color: var(--vue-accent-error);
  margin-left: 2px;
}
.field-error {
  color: var(--vue-accent-error);
  font-size: var(--vue-text-xs);
}
.form-input {
  width: 100%;
}
.form-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid var(--vue-border-soft);
}
@media (max-width: 639px) {
  .form-fields {
    grid-template-columns: 1fr;
  }
}
</style>

```

`src\components\profile\sections\ProjectsSection.vue`:

```vue
<template>
  <div ref="sectionTopRef">
    <ProfileSectionHeader
      :title="$t('profile.projects.title')"
      :purpose="$t('profile.projects.purpose')"
    />

    <EmptyRecordsState
      v-if="records.length === 0 && !formVisible"
      :title="$t('profile.projects.emptyTitle')"
      :hint="$t('profile.projects.emptyHint')"
    />

    <Button
      v-if="!formVisible"
      :label="$t('profile.projects.add')"
      icon="pi pi-plus"
      class="p-button-success p-button-outlined add-btn"
      @click="openAddForm"
    />

    <div v-if="formVisible" ref="formRef" class="inline-form">
      <div class="form-header">
        <h4 class="form-title">{{ editingId ? $t('profile.projects.edit') : $t('profile.projects.add') }}</h4>
      </div>
      <div class="form-fields">
        <div class="form-group">
          <label class="form-label">{{ $t('profile.projects.projectName') }} <span class="required">*</span></label>
          <InputText v-model="formData.projectName" class="form-input" @blur="validateRequiredField('projectName')" />
          <small v-if="formErrors.projectName" class="field-error">{{ formErrors.projectName }}</small>
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('profile.projects.role') }}</label>
          <InputText v-model="formData.role" class="form-input" />
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('profile.projects.startDate') }}</label>
          <DatePicker v-model="formData.startDate" class="form-input" :showIcon="true" :maxDate="formData.endDate || undefined" />
          <small v-if="dateError" class="field-error">{{ dateError }}</small>
        </div>
        <div class="form-group" v-if="!formData.isOngoing">
          <label class="form-label">{{ $t('profile.projects.endDate') }}</label>
          <DatePicker v-model="formData.endDate" class="form-input" :showIcon="true" :minDate="formData.startDate || undefined" />
          <small v-if="dateError" class="field-error">{{ dateError }}</small>
        </div>
        <div class="form-group form-group-checkbox field-full">
          <Checkbox v-model="formData.isOngoing" :binary="true" inputId="proj-ongoing" />
          <label for="proj-ongoing">{{ $t('profile.projects.isOngoing') }}</label>
        </div>
        <div class="form-group field-full">
          <label class="form-label">{{ $t('profile.projects.description') }} <span class="required">*</span></label>
          <Textarea v-model="formData.description" class="form-input" rows="3" @blur="validateRequiredField('description')" />
          <small v-if="formErrors.description" class="field-error">{{ formErrors.description }}</small>
        </div>
        <div class="form-group field-full">
          <label class="form-label">{{ $t('profile.projects.projectUrl') }}</label>
          <InputText v-model="formData.projectUrl" class="form-input" />
        </div>
      </div>
      <div class="form-actions">
        <p class="required-hint">{{ $t('profile.requiredHint') }}</p>
        <Button :label="$t('profile.projects.save')" icon="pi pi-check" class="p-button-success" @click="handleSave" />
        <Button :label="$t('profile.projects.cancel')" class="p-button-text" @click="closeForm" />
      </div>
    </div>

    <RecordCard
      v-for="rec in records"
      :key="rec.id"
      :title="rec.projectName"
      :metaLine="formatMeta(rec)"
      :description="rec.description"
      :url="rec.projectUrl || undefined"
      :chipLabel="rec.isOngoing ? $t('profile.projects.ongoing') : undefined"
      editLabelKey="profile.projects.edit"
      deleteLabelKey="profile.projects.delete"
      @edit="openEditForm(rec)"
      @delete="confirmDelete(rec)"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick, watch } from 'vue'
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'
import { useI18n } from 'vue-i18n'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Textarea from 'primevue/textarea'
import DatePicker from 'primevue/datepicker'
import Checkbox from 'primevue/checkbox'
import { getProjects, saveProjectRecord, deleteProjectRecord } from '@/services/profileMockService'
import type { Project } from '@/types/profile'
import ProfileSectionHeader from '../ProfileSectionHeader.vue'
import RecordCard from '../RecordCard.vue'
import EmptyRecordsState from '../EmptyRecordsState.vue'

const toast = useToast()
const confirm = useConfirm()
const { t, locale } = useI18n()

const emit = defineEmits<{
  saved: []
  dirtyChange: [dirty: boolean]
}>()

const records = ref<Project[]>([])
const formVisible = ref(false)
const editingId = ref<string | null>(null)
const formRef = ref<HTMLElement | null>(null)
const sectionTopRef = ref<HTMLElement | null>(null)
const dateError = ref('')

const formData = reactive({
  projectName: '',
  role: '',
  startDate: null as Date | null,
  endDate: null as Date | null,
  isOngoing: false,
  description: '',
  projectUrl: ''
})

const formErrors = reactive<Record<string, string>>({
  projectName: '',
  description: ''
})

function validateRequiredField(field: string) {
  const val = formData[field as keyof typeof formData]
  formErrors[field as keyof typeof formErrors] = ''
  if (!val) {
    formErrors[field as keyof typeof formErrors] = t('profile.contact.fieldRequired')
  }
}

function validateRequired(): boolean {
  let valid = true
  const fields: (keyof typeof formErrors)[] = ['projectName', 'description']
  for (const f of fields) {
    const val = formData[f as keyof typeof formData] as string
    formErrors[f] = val ? '' : t('profile.contact.fieldRequired')
    if (formErrors[f]) valid = false
  }
  return valid
}

function resetForm() {
  formData.projectName = ''
  formData.role = ''
  formData.startDate = null
  formData.endDate = null
  formData.isOngoing = false
  formData.description = ''
  formData.projectUrl = ''
  editingId.value = null
  dateError.value = ''
  Object.keys(formErrors).forEach(k => formErrors[k as keyof typeof formErrors] = '')
}

function loadRecords() {
  records.value = getProjects()
}

function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const months = locale.value === 'ru'
    ? ['январь', 'февраль', 'март', 'апрель', 'май', 'июнь', 'июль', 'август', 'сентябрь', 'октябрь', 'ноябрь', 'декабрь']
    : ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December']
  return months[d.getMonth()] + ' ' + d.getFullYear()
}

function formatMeta(rec: Project): string {
  const parts: string[] = []
  if (rec.role) parts.push(rec.role)
  const start = formatDate(rec.startDate)
  const end = rec.isOngoing ? t('profile.projects.present') : formatDate(rec.endDate)
  if (start || end) parts.push(start + ' — ' + end)
  return parts.join(' · ')
}

function scrollToSectionTitle() {
  nextTick(() => {
    sectionTopRef.value?.querySelector('.section-title-area')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  })
}

function openAddForm() {
  resetForm()
  formVisible.value = true
  scrollToSectionTitle()
}

function openEditForm(rec: Project) {
  formData.projectName = rec.projectName
  formData.role = rec.role
  formData.startDate = rec.startDate ? new Date(rec.startDate) : null
  formData.endDate = rec.endDate ? new Date(rec.endDate) : null
  formData.isOngoing = rec.isOngoing
  formData.description = rec.description
  formData.projectUrl = rec.projectUrl
  editingId.value = rec.id
  formVisible.value = true
  scrollToSectionTitle()
}

function closeForm() {
  formVisible.value = false
  resetForm()
  emit('dirtyChange', false)
}

function collectFormData(): Project {
  return {
    id: editingId.value || '',
    projectName: formData.projectName,
    role: formData.role,
    startDate: formData.startDate ? formData.startDate.toISOString() : '',
    endDate: formData.isOngoing ? '' : (formData.endDate ? formData.endDate.toISOString() : ''),
    isOngoing: formData.isOngoing,
    description: formData.description,
    projectUrl: formData.projectUrl
  }
}

function validateDates(): boolean {
  dateError.value = ''
  if (formData.startDate && formData.endDate && !formData.isOngoing) {
    if (formData.endDate < formData.startDate) {
      dateError.value = t('profile.dateRangeError')
      return false
    }
  }
  return true
}

function handleSave() {
  if (!validateDates()) return
  if (!validateRequired()) return
  try {
    records.value = saveProjectRecord(collectFormData())
    closeForm()
    emit('saved')
    emit('dirtyChange', false)
    toast.add({ severity: 'success', summary: '', detail: t('profile.saveSuccess'), life: 3000 })
  } catch {
    toast.add({ severity: 'error', summary: '', detail: t('profile.saveError'), life: 3000 })
  }
}

function confirmDelete(rec: Project) {
  confirm.require({
    header: t('profile.deleteConfirm.title'),
    message: t('profile.deleteConfirm.message'),
    rejectLabel: t('profile.deleteConfirm.cancel'),
    acceptLabel: t('profile.deleteConfirm.confirm'),
    accept: () => {
      records.value = deleteProjectRecord(rec.id)
      emit('saved')
      emit('dirtyChange', false)
      toast.add({ severity: 'success', summary: '', detail: t('profile.deleteSuccess'), life: 3000 })
    }
  })
}

watch(formVisible, (visible) => {
  emit('dirtyChange', visible)
})

defineExpose({ loadRecords })

onMounted(loadRecords)
</script>

<style scoped>
.required-hint {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-sm);
  color: var(--vue-text-muted);
  margin: 0 0 8px;
}
.add-btn {
  margin-bottom: 16px;
}
.inline-form {
  background: var(--vue-bg-subtle);
  border: 1px solid var(--vue-border-default);
  border-radius: var(--vue-radius-md);
  padding: 18px 20px;
  margin-bottom: 16px;
}
.form-header {
  margin-bottom: 14px;
}
.form-title {
  font-family: var(--vue-font-heading);
  font-size: var(--vue-text-md);
  font-weight: 600;
  color: var(--vue-text-primary);
  margin: 0;
}
.form-fields {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.form-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.form-group-checkbox {
  flex-direction: row;
  align-items: center;
  gap: 8px;
}
.form-group-checkbox label {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-sm);
  color: var(--vue-text-primary);
  cursor: pointer;
}
.field-full {
  grid-column: 1 / -1;
}
.form-label {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-sm);
  font-weight: 500;
  color: var(--vue-text-primary);
}
.required {
  color: var(--vue-accent-error);
  margin-left: 2px;
}
.field-error {
  color: var(--vue-accent-error);
  font-size: var(--vue-text-xs);
}
.form-input {
  width: 100%;
}
.form-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid var(--vue-border-soft);
}
@media (max-width: 639px) {
  .form-fields {
    grid-template-columns: 1fr;
  }
}
</style>

```

`src\components\profile\sections\WorkExperienceSection.vue`:

```vue
<template>
  <div ref="sectionTopRef">
    <ProfileSectionHeader
      ref="sectionHeaderRef"
      :title="$t('profile.experience.title')"
      :purpose="$t('profile.experience.purpose')"
    />

    <EmptyRecordsState
      v-if="records.length === 0 && !formVisible"
      :title="$t('profile.experience.emptyTitle')"
      :hint="$t('profile.experience.emptyHint')"
    />

    <Button
      v-if="!formVisible"
      :label="$t('profile.experience.add')"
      icon="pi pi-plus"
      class="p-button-success p-button-outlined add-btn"
      @click="openAddForm"
    />

    <div v-if="formVisible" ref="formRef" class="inline-form">
      <div class="form-header">
        <h4 class="form-title">{{ editingId ? $t('profile.experience.edit') : $t('profile.experience.add') }}</h4>
      </div>
      <div class="form-fields">
        <div class="form-group">
          <label class="form-label">{{ $t('profile.experience.jobTitle') }} <span class="required">*</span></label>
          <InputText v-model="formData.jobTitle" class="form-input" @blur="validateRequiredField('jobTitle')" />
          <small v-if="formErrors.jobTitle" class="field-error">{{ formErrors.jobTitle }}</small>
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('profile.experience.companyName') }} <span class="required">*</span></label>
          <InputText v-model="formData.companyName" class="form-input" @blur="validateRequiredField('companyName')" />
          <small v-if="formErrors.companyName" class="field-error">{{ formErrors.companyName }}</small>
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('profile.experience.location') }}</label>
          <InputText v-model="formData.location" class="form-input" :placeholder="$t('profile.experience.locationPlaceholder')" />
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('profile.experience.companyUrl') }}</label>
          <InputText v-model="formData.companyUrl" class="form-input" />
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('profile.experience.startDate') }} <span class="required">*</span></label>
          <DatePicker v-model="formData.startDate" class="form-input" :showIcon="true" :maxDate="formData.endDate || undefined" />
          <small v-if="dateError" class="field-error">{{ dateError }}</small>
          <small v-if="formErrors.startDate" class="field-error">{{ formErrors.startDate }}</small>
        </div>
        <div class="form-group" v-if="!formData.currentlyWorkHere">
          <label class="form-label">{{ $t('profile.experience.endDate') }}</label>
          <DatePicker v-model="formData.endDate" class="form-input" :showIcon="true" :minDate="formData.startDate || undefined" />
          <small v-if="dateError" class="field-error">{{ dateError }}</small>
        </div>
        <div class="form-group form-group-checkbox field-full">
          <Checkbox v-model="formData.currentlyWorkHere" :binary="true" inputId="we-current" />
          <label for="we-current">{{ $t('profile.experience.currentlyWorkHere') }}</label>
        </div>
        <div class="form-group field-full">
          <label class="form-label">{{ $t('profile.experience.description') }} <span class="required">*</span></label>
          <Textarea v-model="formData.description" class="form-input" rows="3" @blur="validateRequiredField('description')" />
          <small v-if="formErrors.description" class="field-error">{{ formErrors.description }}</small>
        </div>
      </div>
      <div class="form-actions">
        <p class="required-hint">{{ $t('profile.requiredHint') }}</p>
        <Button :label="$t('profile.experience.save')" icon="pi pi-check" class="p-button-success" @click="handleSave" />
        <Button :label="$t('profile.experience.cancel')" class="p-button-text" @click="closeForm" />
      </div>
    </div>

    <RecordCard
      v-for="rec in records"
      :key="rec.id"
      :title="rec.jobTitle"
      :metaLine="formatMeta(rec)"
      :description="rec.description"
      :url="rec.companyUrl || undefined"
      :chipLabel="rec.currentlyWorkHere ? $t('profile.experience.current') : undefined"
      editLabelKey="profile.experience.edit"
      deleteLabelKey="profile.experience.delete"
      @edit="openEditForm(rec)"
      @delete="confirmDelete(rec)"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick, watch } from 'vue'
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'
import { useI18n } from 'vue-i18n'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Textarea from 'primevue/textarea'
import DatePicker from 'primevue/datepicker'
import Checkbox from 'primevue/checkbox'
import { getWorkExperience, saveWorkExperienceRecord, deleteWorkExperienceRecord } from '@/services/profileMockService'
import type { WorkExperience } from '@/types/profile'
import ProfileSectionHeader from '../ProfileSectionHeader.vue'
import RecordCard from '../RecordCard.vue'
import EmptyRecordsState from '../EmptyRecordsState.vue'

const toast = useToast()
const confirm = useConfirm()
const { t, locale } = useI18n()

const emit = defineEmits<{
  saved: []
  dirtyChange: [dirty: boolean]
}>()

const records = ref<WorkExperience[]>([])
const formVisible = ref(false)
const editingId = ref<string | null>(null)
const formRef = ref<HTMLElement | null>(null)
const sectionTopRef = ref<HTMLElement | null>(null)
const sectionHeaderRef = ref<InstanceType<typeof ProfileSectionHeader> | null>(null)
const dateError = ref('')

const formData = reactive({
  jobTitle: '',
  companyName: '',
  location: '',
  startDate: null as Date | null,
  endDate: null as Date | null,
  currentlyWorkHere: false,
  description: '',
  companyUrl: ''
})

const formErrors = reactive<Record<string, string>>({
  jobTitle: '',
  companyName: '',
  startDate: '',
  description: ''
})

const requiredFields = ['jobTitle', 'companyName', 'startDate', 'description'] as const

function validateRequiredField(field: string) {
  const val = formData[field as keyof typeof formData]
  formErrors[field as keyof typeof formErrors] = ''
  if (!val) {
    formErrors[field as keyof typeof formErrors] = t('profile.contact.fieldRequired')
  }
}

function validateRequired(): boolean {
  let valid = true
  for (const f of requiredFields) {
    validateRequiredField(f)
    if (formErrors[f]) valid = false
  }
  return valid
}

function resetForm() {
  formData.jobTitle = ''
  formData.companyName = ''
  formData.location = ''
  formData.startDate = null
  formData.endDate = null
  formData.currentlyWorkHere = false
  formData.description = ''
  formData.companyUrl = ''
  editingId.value = null
  dateError.value = ''
  Object.keys(formErrors).forEach(k => formErrors[k as keyof typeof formErrors] = '')
}

function loadRecords() {
  records.value = getWorkExperience()
}

function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const months = locale.value === 'ru'
    ? ['январь', 'февраль', 'март', 'апрель', 'май', 'июнь', 'июль', 'август', 'сентябрь', 'октябрь', 'ноябрь', 'декабрь']
    : ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December']
  return months[d.getMonth()] + ' ' + d.getFullYear()
}

function formatMeta(rec: WorkExperience): string {
  const parts = [rec.companyName]
  const start = formatDate(rec.startDate)
  const end = rec.currentlyWorkHere ? t('profile.experience.present') : formatDate(rec.endDate)
  if (start || end) parts.push(start + ' — ' + end)
  if (rec.location) parts.push(rec.location)
  return parts.join(' · ')
}

function scrollToSectionTitle() {
  nextTick(() => {
    sectionTopRef.value?.querySelector('.section-title-area')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  })
}

function openAddForm() {
  resetForm()
  formVisible.value = true
  scrollToSectionTitle()
}

function openEditForm(rec: WorkExperience) {
  formData.jobTitle = rec.jobTitle
  formData.companyName = rec.companyName
  formData.location = rec.location
  formData.startDate = rec.startDate ? new Date(rec.startDate) : null
  formData.endDate = rec.endDate ? new Date(rec.endDate) : null
  formData.currentlyWorkHere = rec.currentlyWorkHere
  formData.description = rec.description
  formData.companyUrl = rec.companyUrl
  editingId.value = rec.id
  formVisible.value = true
  scrollToSectionTitle()
}

function closeForm() {
  formVisible.value = false
  resetForm()
  emit('dirtyChange', false)
}

function collectFormData(): WorkExperience {
  return {
    id: editingId.value || '',
    jobTitle: formData.jobTitle,
    companyName: formData.companyName,
    location: formData.location,
    startDate: formData.startDate ? formData.startDate.toISOString() : '',
    endDate: formData.currentlyWorkHere ? '' : (formData.endDate ? formData.endDate.toISOString() : ''),
    currentlyWorkHere: formData.currentlyWorkHere,
    description: formData.description,
    companyUrl: formData.companyUrl
  }
}

function validateDates(): boolean {
  dateError.value = ''
  if (formData.startDate && formData.endDate && !formData.currentlyWorkHere) {
    if (formData.endDate < formData.startDate) {
      dateError.value = t('profile.dateRangeError')
      return false
    }
  }
  return true
}

function handleSave() {
  if (!validateDates()) return
  if (!validateRequired()) return
  try {
    records.value = saveWorkExperienceRecord(collectFormData())
    closeForm()
    emit('saved')
    emit('dirtyChange', false)
    toast.add({ severity: 'success', summary: '', detail: t('profile.saveSuccess'), life: 3000 })
  } catch {
    toast.add({ severity: 'error', summary: '', detail: t('profile.saveError'), life: 3000 })
  }
}

function confirmDelete(rec: WorkExperience) {
  confirm.require({
    header: t('profile.deleteConfirm.title'),
    message: t('profile.deleteConfirm.message'),
    rejectLabel: t('profile.deleteConfirm.cancel'),
    acceptLabel: t('profile.deleteConfirm.confirm'),
    accept: () => {
      records.value = deleteWorkExperienceRecord(rec.id)
      emit('saved')
      emit('dirtyChange', false)
      toast.add({ severity: 'success', summary: '', detail: t('profile.deleteSuccess'), life: 3000 })
    }
  })
}

watch(formVisible, (visible) => {
  emit('dirtyChange', visible)
})

defineExpose({ loadRecords })

onMounted(loadRecords)
</script>

<style scoped>
.required-hint {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-sm);
  color: var(--vue-text-muted);
  margin: 0 0 8px;
}
.add-btn {
  margin-bottom: 16px;
}
.inline-form {
  background: var(--vue-bg-subtle);
  border: 1px solid var(--vue-border-default);
  border-radius: var(--vue-radius-md);
  padding: 18px 20px;
  margin-bottom: 16px;
}
.form-header {
  margin-bottom: 14px;
}
.form-title {
  font-family: var(--vue-font-heading);
  font-size: var(--vue-text-md);
  font-weight: 600;
  color: var(--vue-text-primary);
  margin: 0;
}
.form-fields {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.form-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.form-group-checkbox {
  flex-direction: row;
  align-items: center;
  gap: 8px;
}
.form-group-checkbox label {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-sm);
  color: var(--vue-text-primary);
  cursor: pointer;
}
.field-full {
  grid-column: 1 / -1;
}
.form-label {
  font-family: var(--vue-font-body);
  font-size: var(--vue-text-sm);
  font-weight: 500;
  color: var(--vue-text-primary);
}
.required {
  color: var(--vue-accent-error);
  margin-left: 2px;
}
.field-error {
  color: var(--vue-accent-error);
  font-size: var(--vue-text-xs);
}
.form-input {
  width: 100%;
}
.form-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid var(--vue-border-soft);
}
@media (max-width: 639px) {
  .form-fields {
    grid-template-columns: 1fr;
  }
}
</style>

```

`src\composables\useAuth.ts`:

```ts
import { ref } from 'vue'
import * as authService from '@/services/authService'

const role = ref<string | null>('USER')
const authenticated = ref(true)

export function useAuth() {
  async function logout() {
    await authService.logout()
    role.value = null
    authenticated.value = false
  }

  return { role, authenticated, logout }
}

```

`src\composables\useUserHome.ts`:

```ts
import { ref } from 'vue'

export function useUserHome() {
  const summary = ref(null)
  const summaryLoading = ref(false)
  const summaryError = ref('')
  const resumes = ref([])
  const totalRecords = ref(0)
  const tableLoading = ref(false)
  const tableError = ref('')
  const queryParams = ref({ page: 0, size: 10, sort: 'createdAt,desc' })
  const selectedResume = ref(null)
  const modalVisible = ref(false)

  async function fetchAll() {}
  async function loadSummary() {}
  async function loadResumes() {}
  function onPage(e: any) {}
  function onSort(e: any) {}
  function onFilter(e: any) {}
  function onSearch(q: string) {}
  function openResumeModal(r: any) {
    selectedResume.value = r
    modalVisible.value = true
  }
  function closeModal() { modalVisible.value = false }
  async function handleDelete(id: number) {}

  return {
    summary, summaryLoading, summaryError,
    resumes, totalRecords, tableLoading, tableError,
    queryParams, selectedResume, modalVisible,
    fetchAll, loadSummary, loadResumes,
    onPage, onSort, onFilter, onSearch,
    openResumeModal, closeModal, handleDelete
  }
}

```

`src\env.d.ts`:

```ts
/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

```

`src\i18n\en.json`:

```json
{
  "auth": {
    "login": "Log in",
    "register": "Register",
    "email": "Email",
    "password": "Password",
    "confirmPassword": "Confirm password",
    "rememberMe": "Remember me",
    "loginTitle": "Welcome back",
    "registerTitle": "Create account",
    "loginDescription": "Sign in to your account",
    "registerDescription": "Create your account",
    "loginInfo": "One profile. Tailored resumes for any role.\nSign in to continue your career journey.",
    "registerInfo": "One profile. Tailored resumes for any role.\nCreate your account and get started.",
    "loginLink": "Already registered?",
    "registerLink": "Don't have an account?",
    "error": {
      "invalidCredentials": "Invalid email or password",
      "emailRequired": "Email is required",
      "emailInvalid": "Please enter a valid email address",
      "passwordRequired": "Password is required",
      "passwordMinLength": "Password must be at least 8 characters",
      "passwordStrength": "Password must contain uppercase, lowercase, and digit",
      "confirmPasswordRequired": "Please confirm your password",
      "passwordMismatch": "Passwords do not match",
      "emailTaken": "An account with this email already exists",
      "accountBlocked": "Your account is inactive. Contact support for assistance.",
      "rateLimited": "Too many failed attempts. Try again later.",
      "serverError": "Something went wrong. Please try again."
    },
    "success": {
      "registered": "Account created successfully!",
      "loggedOut": "Logged out successfully"
    }
  },
  "home": {
    "title": "Resume workspace",
    "incomplete": {
      "title": "Complete your profile first",
      "text": "AI needs context about you. Add at least your contact details, work experience, and education. The more useful information you provide, the better resume you will get.",
      "cta": "Complete Profile"
    },
    "checklist": {
      "contact": "Contact details",
      "experience": "Work experience",
      "education": "Education",
      "done": "Done",
      "missing": "Missing"
    },
    "ready": {
      "title": "Your next best step",
      "generate": {
        "title": "Generate your next resume",
        "hint": "Start with a vacancy and get an adapted resume.",
        "cta": "Generate Resume",
        "tooltip": "Make a new awesome resume for a specific vacancy."
      },
      "update": {
        "title": "Update your profile",
        "hint": "Use this when your experience, education, skills, or contact details change.",
        "cta": "Update Profile",
        "tooltip": "Edit the profile data used for future resumes."
      }
    },
    "summary": {
      "savedResumes": "Saved resumes",
      "profileStatus": "Profile status",
      "ready": "Ready",
      "needsInfo": "Needs info",
      "readyHint": "Go on with making strong resumes — sky is the limit!",
      "needsInfoHint": "Add contact details, work experience, and education.",
      "incompleteResumesHint": "No resumes here so far. To make them, fill your profile first and add the required information.",
      "emptyResumesHint": "Make your first strong resume — it\u0027s really nice and easy!",
      "emptyStatusHint": "Start making awesome resumes — it\u0027s all in your hands!",
      "readyStatusHint": "Profile is ready! But you can always update it and make it even better!",
      "updateProfile": "Update profile",
      "completeProfile": "Complete profile",
      "lastResume": "Last resume",
      "noLastResume": "No resumes yet"
    },
    "table": {
      "title": "Saved resumes",
      "searchPlaceholder": "Search by title, vacancy, or company",
      "search": "Search",
      "clear": "Clear",
      "clearTooltip": "Clear all filters",
      "resumeTitle": "Resume title",
      "vacancy": "Vacancy",
      "company": "Company",
      "language": "Language",
      "adaptationLevel": "Adaptation level",
      "created": "Created",
      "filterLanguage": "Language",
      "filterAdaptation": "Adaptation",
      "filterDate": "Created date",
      "dateFrom": "From",
      "dateTo": "To",
      "loading": "Loading resumes. Please wait.",
      "emptyTitle": "No resumes yet",
      "noResultsTitle": "No resumes found",
      "noResultsText": "Try another search or change filters.",
      "pageReport": "Showing {first} to {last} of {totalRecords}",
      "mobilePageReport": "Page {current} of {totalPages}",
      "sortNotSorted": "Sort ascending",
      "sortAsc": "Sort descending",
      "sortDesc": "Clear sorting for this column"
    }
  },
  "resumeDetails": {
    "title": "Resume details",
    "publicLink": "Public link",
    "view": "View",
    "downloadPdf": "Download PDF",
    "copyLink": "Copy link",
    "delete": "Delete",
    "coverLetter": "Cover letter",
    "copyCoverLetter": "Copy cover letter",
    "noCoverLetter": "No cover letter for this resume.",
    "copied": "Copied.",
    "linkCopied": "Link copied.",
    "coverLetterCopied": "Cover letter copied."
  },
  "deleteResume": {
    "title": "Delete resume?",
    "text": "This resume will no longer be available from your workspace or public link.",
    "cancel": "Cancel",
    "confirm": "Delete",
    "success": "Resume deleted."
  },
  "generate": {
    "steps": {
      "vacancy": "Vacancy",
      "settings": "Settings",
      "review": "Review",
      "export": "Export"
    },
    "disabledTooltip": "Generate a resume first.",
    "loadingTitle": "Generating your resume...",
    "loadingText": "This may take a moment. Please keep this page open."
  },
  "placeholder": {
    "title": "Placeholder page",
    "text": "This page is a placeholder for future work.",
    "back": "Back to Home"
  },
  "language": {
    "en": "English",
    "ru": "Russian"
  },
  "adaptation": {
    "minimal": "Minimal",
    "balanced": "Balanced",
    "maximum": "Maximum"
  },
  "profile": {
    "title": "My Profile",
    "subnav": {
      "contact": "Contacts",
      "experience": "Experience",
      "education": "Education",
      "projects": "Projects",
      "courses": "Courses",
      "additional": "Additional"
    },
    "status": {
      "completed": "Completed ✓",
      "incomplete": "Incomplete !",
      "noRecords": "No records",
      "records": "{count} records",
      "record": "{count} record",
      "noRecordsShort": "0 records"
    },
    "unsaved": {
      "title": "Leave without saving?",
      "message": "You have unsaved changes.",
      "leave": "Leave without saving",
      "stay": "Stay on this page"
    },
    "saveSuccess": "Saved successfully",
    "saveError": "Could not save changes. Please try again.",
    "deleteSuccess": "Record deleted",
    "requiredHint": "Fields marked with * are required.",
    "emptyRecords": "No records yet",
    "dateRangeError": "End date must be after start date.",
    "contact": {
      "title": "Contact Details",
      "purpose": "Add the contact information that can be safely included in your resume. Only selected contact fields will be shown in generated resume versions.",
      "fullName": "Full name",
      "professionalTitle": "Professional title",
      "email": "Email",
      "phone": "Phone",
      "location": "Location",
      "linkedinUrl": "LinkedIn URL",
      "portfolioUrl": "Portfolio / Website URL",
      "telegram": "Telegram",
      "whatsapp": "WhatsApp",
      "save": "Save",
      "fieldRequired": "This field is required.",
      "emailRequired": "Email is required.",
      "emailInvalid": "Please enter a valid email address.",
      "urlInvalid": "Please enter a valid URL.",
      "fullNamePlaceholder": "John Doe",
      "professionalTitlePlaceholder": "Business Analyst, Junior Java Developer",
      "emailPlaceholder": "johndoe@example.com",
      "phonePlaceholder": "+7-777-777-77-77",
      "locationPlaceholder": "Kazakhstan, Astana",
      "linkedinPlaceholder": "https://www.linkedin.com/in/john/",
      "portfolioPlaceholder": "https://johndoe.portfolio.com",
      "telegramPlaceholder": "@johndoe",
      "whatsappPlaceholder": "+7-777-777-77-77"
    },
    "experience": {
      "title": "Work Experience",
      "purpose": "Add your recent and relevant work experience. Focus on responsibilities, achievements, tools, and measurable results.",
      "add": "Add work experience",
      "edit": "Edit",
      "delete": "Delete",
      "save": "Save",
      "cancel": "Cancel",
      "jobTitle": "Job title",
      "companyName": "Company name",
      "location": "Location",
      "locationPlaceholder": "Kazakhstan, Astana",
      "startDate": "Start date",
      "endDate": "End date",
      "currentlyWorkHere": "I currently work here",
      "description": "Role and job description",
      "companyUrl": "Company URL",
      "present": "Present",
      "current": "Current",
      "emptyTitle": "No records yet",
      "emptyHint": "No work experience added yet. Add relevant roles to help AI generate stronger resumes."
    },
    "projects": {
      "title": "Projects & Volunteering",
      "purpose": "Add personal, academic, professional, or volunteering projects that demonstrate your skills, initiative, and practical experience.",
      "add": "Add project",
      "edit": "Edit",
      "delete": "Delete",
      "save": "Save",
      "cancel": "Cancel",
      "projectName": "Project name",
      "role": "Role",
      "startDate": "Start date",
      "endDate": "End date",
      "isOngoing": "This project is ongoing",
      "description": "Description",
      "projectUrl": "Project URL",
      "present": "Present",
      "ongoing": "Ongoing",
      "emptyTitle": "No records yet",
      "emptyHint": "No projects added yet. Add practical projects that can support your resume positioning."
    },
    "education": {
      "title": "Education",
      "purpose": "Add education records that should be considered when generating resumes.",
      "add": "Add education",
      "edit": "Edit",
      "delete": "Delete",
      "save": "Save",
      "cancel": "Cancel",
      "institutionName": "Institution name",
      "degree": "Degree / Qualification",
      "fieldOfStudy": "Field of study / Major",
      "startDate": "Start date",
      "endDate": "End date",
      "currentlyStudying": "I am currently studying here",
      "location": "Location",
      "comment": "Comment / Description",
      "gpa": "GPA / Grade",
      "present": "Present",
      "current": "Current",
      "emptyTitle": "No records yet",
      "emptyHint": "No education added yet. Add your education information to help improve generated resumes."
    },
    "courses": {
      "title": "Courses & Certificates",
      "purpose": "Add courses and certificates that can strengthen generated resumes and cover letters.",
      "add": "Add Course",
      "edit": "Edit",
      "delete": "Delete",
      "save": "Save",
      "cancel": "Cancel",
      "addTitle": "Add Course",
      "editTitle": "Edit Course",
      "detailsTitle": "Course Details",
      "courseName": "Course / Certificate name",
      "provider": "Provider / Issuer",
      "startDate": "Start Date",
      "endDate": "End Date",
      "credentialUrl": "Credential URL",
      "skills": "Skills / Topics",
      "description": "Short description",
      "courseColumn": "Course",
      "providerColumn": "Provider",
      "skillsColumn": "Skills",
      "searchPlaceholder": "Search courses, providers, skills",
      "dateFrom": "From",
      "dateTo": "To",
      "emptyTitle": "No records yet",
      "emptyHint": "No courses or certificates added yet. Add learning records that may support your future resumes.",
      "noRecordsStatus": "No records",
      "rowsPerPage": "Rows per page",
      "pageReport": "Showing {first} to {last} of {totalRecords}",
      "resetFilters": "Reset",
      "dateFilterError": "End date must be after start date",
      "searchMinChars": "Enter at least 3 characters to search"
    },
    "additional": {
      "title": "Additional Info",
      "save": "Save",
      "block1Title": "Resume & Public Profile Preferences",
      "block2Title": "Work Preferences",
      "block3Title": "Professional Info",
      "block4Title": "Personal Info",
      "username": "Username",
      "usernamePlaceholder": "yourname",
      "usernameHelp": "This username will be used as part of your public resume link, for example: resumainer.com/yourusername/YRFJ",
      "usernameInvalid": "Username must contain only English letters, digits, hyphens and underscores.",
      "defaultResumeLanguage": "Default resume language",
      "additionalResumeLanguage": "Additional resume language",
      "languageEnglish": "English",
      "languageRussian": "Russian",
      "acceptableWorkFormats": "Acceptable work formats",
      "willingnessToRelocate": "Willingness to relocate",
      "willingnessForBusinessTravel": "Willingness for business travel",
      "office": "Office",
      "remote": "Remote",
      "hybrid": "Hybrid",
      "relocation": "Relocation",
      "rotationalSchedule": "Rotational schedule",
      "yes": "Yes",
      "no": "No",
      "negotiable": "Negotiable",
      "skills": "Skills",
      "skillsPlaceholder": "Example: business analysis, stakeholder communication, SQL, Excel, process modeling",
      "spokenLanguages": "Spoken languages",
      "spokenLanguagesPlaceholder": "English C1, Russian native",
      "professionalAspirations": "Professional aspirations",
      "professionalAspirationsPlaceholder": "Example: I want to grow into a backend developer role and work on products that solve real business problems.",
      "achievements": "Achievements",
      "achievementsPlaceholder": "Example: improved reporting process, prepared requirements for 50+ business cases, automated routine analysis tasks.",
      "additionalContextForAI": "Additional context for AI",
      "additionalContextForAIPlaceholder": "Example: I prefer concise resumes, want to highlight analytical thinking, and want to avoid overstating my experience.",
      "additionalContextHint": "Add job-related context that may help AI adapt your resume. Do not add private information that should not appear in resumes.",
      "dateOfBirth": "Date of birth",
      "citizenship": "Citizenship"
    },
    "deleteConfirm": {
      "title": "Delete record?",
      "message": "This action cannot be undone.",
      "cancel": "Cancel",
      "confirm": "Delete"
    }
  },
  "common": {
    "loading": "Loading..."
  },
  "nav": {
    "home": "Home",
    "myProfile": "My Profile",
    "generateResume": "Generate Resume",
    "admin": "Admin",
    "language": "Language",
    "logout": "Log out",
    "mobileMenu": "Menu"
  }
}

```

`src\i18n\index.ts`:

```ts
import { createI18n } from 'vue-i18n'
import en from './en.json'
import ru from './ru.json'

const savedLocale = localStorage.getItem('locale') || 'en'

const i18n = createI18n({
  locale: savedLocale,
  fallbackLocale: 'en',
  messages: {
    en,
    ru
  }
})

export default i18n

```

`src\i18n\ru.json`:

```json
{
  "auth": {
    "login": "Войти",
    "register": "Регистрация",
    "email": "Email",
    "password": "Пароль",
    "confirmPassword": "Подтверди пароль",
    "rememberMe": "Запомнить меня",
    "loginTitle": "С возвращением",
    "registerTitle": "Создать аккаунт",
    "loginDescription": "Войди в аккаунт",
    "registerDescription": "Создай аккаунт",
    "loginInfo": "Один профиль. Резюме под любую роль.\nВойди, чтобы продолжить.",
    "registerInfo": "Один профиль. Резюме под любую роль.\nСоздай аккаунт и начни.",
    "loginLink": "Уже зарегистрирован?",
    "registerLink": "Нет аккаунта?",
    "error": {
      "invalidCredentials": "Неверный email или пароль",
      "emailRequired": "Email обязателен",
      "emailInvalid": "Введи корректный email",
      "passwordRequired": "Пароль обязателен",
      "passwordMinLength": "Пароль должен быть минимум 8 символов",
      "passwordStrength": "Пароль должен содержать заглавные, строчные буквы и цифру",
      "confirmPasswordRequired": "Подтверди пароль",
      "passwordMismatch": "Пароли не совпадают",
      "emailTaken": "Аккаунт с таким email уже существует",
      "accountBlocked": "Аккаунт неактивен. Обратись в поддержку.",
      "rateLimited": "Слишком много попыток. Попробуй позже.",
      "serverError": "Что-то пошло не так. Попробуй снова."
    },
    "success": {
      "registered": "Аккаунт создан!",
      "loggedOut": "Вышел из аккаунта"
    }
  },
  "home": {
    "title": "Рабочий центр",
    "incomplete": {
      "title": "Сначала заполни профиль",
      "text": "ИИ нужен контекст о тебе. Добавь хотя бы контакты, опыт работы и образование. Чем больше полезной информации ты укажешь, тем точнее получится резюме.",
      "cta": "Заполнить профиль"
    },
    "checklist": {
      "contact": "Контакты",
      "experience": "Опыт работы",
      "education": "Образование",
      "done": "Готово",
      "missing": "Не заполнено"
    },
    "ready": {
      "title": "Следующий лучший шаг",
      "generate": {
        "title": "Создай новое резюме",
        "hint": "Начни с вакансии и получи адаптированное резюме.",
        "cta": "Создать резюме",
        "tooltip": "Создать сильное резюме под конкретную вакансию."
      },
      "update": {
        "title": "Обнови профиль",
        "hint": "Используй, если изменились опыт, образование, навыки или контакты.",
        "cta": "Обновить профиль",
        "tooltip": "Изменить данные, которые будут использоваться в будущих резюме."
      }
    },
    "summary": {
      "savedResumes": "Сохранённые резюме",
      "profileStatus": "Статус профиля",
      "ready": "Готов",
      "needsInfo": "Нужно заполнить",
      "readyHint": "Продолжай создавать отличные резюме — нет ничего невозможного!",
      "needsInfoHint": "Добавь контакты, опыт работы и образование.",
      "incompleteResumesHint": "Пока резюме тут нет. Чтобы их создать — сначала заполни профиль и добавь нужную информацию.",
      "emptyResumesHint": "Создай своё первое сильное резюме — это легко и просто!",
      "emptyStatusHint": "Начни создавать отличные резюме — всё в твоих руках!",
      "readyStatusHint": "Профиль готов! Но всегда можешь его обновить и сделать ещё лучше!",
      "updateProfile": "Обновить профиль",
      "completeProfile": "Заполнить профиль",
      "lastResume": "Последнее резюме",
      "noLastResume": "Резюме пока нет"
    },
    "table": {
      "title": "Сохранённые резюме",
      "searchPlaceholder": "Найти по названию, вакансии или компании",
      "search": "Найти",
      "clear": "Сброс",
      "clearTooltip": "Сбросить все фильтры",
      "resumeTitle": "Название резюме",
      "vacancy": "Вакансия",
      "company": "Компания",
      "language": "Язык",
      "adaptationLevel": "Уровень адаптации",
      "created": "Создано",
      "filterLanguage": "Язык",
      "filterAdaptation": "Адаптация",
      "filterDate": "Дата создания",
      "dateFrom": "С",
      "dateTo": "По",
      "loading": "Загружаем резюме. Подожди немного.",
      "emptyTitle": "Резюме пока нет",
      "noResultsTitle": "Ничего не найдено",
      "noResultsText": "Попробуй другой запрос или измени фильтры.",
      "pageReport": "Показано {first}–{last} из {totalRecords}",
      "mobilePageReport": "Страница {current} из {totalPages}",
      "sortNotSorted": "Сортировать по возрастанию",
      "sortAsc": "Сортировать по убыванию",
      "sortDesc": "Сбросить сортировку по этой колонке"
    }
  },
  "resumeDetails": {
    "title": "Детали резюме",
    "publicLink": "Публичная ссылка",
    "view": "Открыть",
    "downloadPdf": "Скачать PDF",
    "copyLink": "Скопировать ссылку",
    "delete": "Удалить",
    "coverLetter": "Сопроводительное письмо",
    "copyCoverLetter": "Скопировать письмо",
    "noCoverLetter": "Для этого резюме сопроводительное письмо не создано.",
    "copied": "Скопировано.",
    "linkCopied": "Ссылка скопирована.",
    "coverLetterCopied": "Сопроводительное письмо скопировано."
  },
  "deleteResume": {
    "title": "Удалить резюме?",
    "text": "Это резюме исчезнет из рабочего центра, а публичная ссылка перестанет работать.",
    "cancel": "Отмена",
    "confirm": "Удалить",
    "success": "Резюме удалено."
  },
  "generate": {
    "steps": {
      "vacancy": "Вакансия",
      "settings": "Настройки",
      "review": "Проверка",
      "export": "Экспорт"
    },
    "disabledTooltip": "Сначала создай резюме.",
    "loadingTitle": "Создаём резюме...",
    "loadingText": "Это может занять немного времени. Не закрывай страницу."
  },
  "placeholder": {
    "title": "Страница-заглушка",
    "text": "Это страница-заглушка для будущей работы.",
    "back": "На главную"
  },
  "nav": {
    "home": "Главная",
    "myProfile": "Профиль",
    "generateResume": "Создать резюме",
    "admin": "Админ",
    "language": "Язык",
    "logout": "Выйти",
    "mobileMenu": "Меню"
  },
  "language": {
    "en": "Английский",
    "ru": "Русский"
  },
  "adaptation": {
    "minimal": "Минимальная",
    "balanced": "Сбалансированная",
    "maximum": "Максимальная"
  },
  "profile": {
    "title": "Мой профиль",
    "subnav": {
      "contact": "Контакты",
      "experience": "Опыт работы",
      "education": "Образование",
      "projects": "Проекты",
      "courses": "Курсы",
      "additional": "Дополнительно"
    },
    "status": {
      "completed": "Заполнено ✓",
      "incomplete": "Не заполнен !",
      "noRecords": "Нет записей",
      "records": "{count} записи",
      "record": "{count} запись",
      "noRecordsShort": "0 записей"
    },
    "unsaved": {
      "title": "Выйти без сохранения?",
      "message": "Есть несохранённые изменения.",
      "leave": "Выйти без сохранения",
      "stay": "Остаться на странице"
    },
    "saveSuccess": "Сохранено",
    "saveError": "Не удалось сохранить изменения. Попробуй ещё раз.",
    "deleteSuccess": "Запись удалена",
    "requiredHint": "Поля, отмеченные *, обязательны.",
    "emptyRecords": "Пока нет записей",
    "dateRangeError": "Дата окончания должна быть позже даты начала.",
    "contact": {
      "title": "Контактные данные",
      "purpose": "Добавь контактные данные, которые можно безопасно включать в резюме. В созданные версии резюме попадут только заполненные поля.",
      "fullName": "Полное имя",
      "professionalTitle": "Профессия",
      "email": "Email",
      "phone": "Телефон",
      "location": "Локация",
      "linkedinUrl": "Ссылка на LinkedIn",
      "portfolioUrl": "Портфолио / личный сайт",
      "telegram": "Telegram",
      "whatsapp": "WhatsApp",
      "save": "Сохранить",
      "fieldRequired": "Это поле обязательно.",
      "emailRequired": "Email обязателен.",
      "emailInvalid": "Введи корректный email.",
      "urlInvalid": "Введи корректный URL.",
      "fullNamePlaceholder": "Иван Иванов",
      "professionalTitlePlaceholder": "Бизнес-аналитик, Junior Java-разработчик",
      "emailPlaceholder": "ivan@example.com",
      "phonePlaceholder": "+7-777-777-77-77",
      "locationPlaceholder": "Казахстан, Астана",
      "linkedinPlaceholder": "https://www.linkedin.com/in/ivan/",
      "portfolioPlaceholder": "https://ivan.portfolio.com",
      "telegramPlaceholder": "@ivanov",
      "whatsappPlaceholder": "+7-777-777-77-77"
    },
    "experience": {
      "title": "Опыт работы",
      "purpose": "Добавь недавний и релевантный опыт работы. Сфокусируйся на обязанностях, достижениях, инструментах и измеримых результатах.",
      "add": "Добавить опыт работы",
      "edit": "Изменить",
      "delete": "Удалить",
      "save": "Сохранить",
      "cancel": "Отмена",
      "jobTitle": "Должность",
      "companyName": "Компания",
      "location": "Локация",
      "locationPlaceholder": "Казахстан, Астана",
      "startDate": "Дата начала",
      "endDate": "Дата окончания",
      "currentlyWorkHere": "Это моё текущее место работы",
      "description": "Описание роли и работы",
      "companyUrl": "Сайт компании",
      "present": "по настоящее время",
      "current": "Текущее",
      "emptyTitle": "Пока нет записей",
      "emptyHint": "Опыт работы пока не добавлен. Добавь релевантные роли, чтобы AI мог создавать более сильные резюме."
    },
    "projects": {
      "title": "Проекты и волонтёрство",
      "purpose": "Добавь личные, учебные, профессиональные или волонтёрские проекты, которые показывают твои навыки, инициативу и практический опыт.",
      "add": "Добавить проект",
      "edit": "Изменить",
      "delete": "Удалить",
      "save": "Сохранить",
      "cancel": "Отмена",
      "projectName": "Название проекта",
      "role": "Роль",
      "startDate": "Дата начала",
      "endDate": "Дата окончания",
      "isOngoing": "Проект продолжается",
      "description": "Описание",
      "projectUrl": "Ссылка на проект",
      "present": "по настоящее время",
      "ongoing": "Продолжается",
      "emptyTitle": "Пока нет записей",
      "emptyHint": "Проекты пока не добавлены. Добавь практические проекты, которые помогут усилить позиционирование в резюме."
    },
    "education": {
      "title": "Образование",
      "purpose": "Добавь образование, которое нужно учитывать при создании резюме.",
      "add": "Добавить образование",
      "edit": "Изменить",
      "delete": "Удалить",
      "save": "Сохранить",
      "cancel": "Отмена",
      "institutionName": "Учебное заведение",
      "degree": "Степень / квалификация",
      "fieldOfStudy": "Направление / специальность",
      "startDate": "Дата начала",
      "endDate": "Дата окончания",
      "currentlyStudying": "Я ещё не выпустился и учусь здесь сейчас",
      "location": "Локация",
      "comment": "Комментарий / описание",
      "gpa": "GPA / оценка",
      "present": "по настоящее время",
      "current": "Текущее",
      "emptyTitle": "Пока нет записей",
      "emptyHint": "Образование пока не добавлено. Добавь информацию об имеющемся образовании, это поможет улучшить резюме."
    },
    "courses": {
      "title": "Курсы и сертификаты",
      "purpose": "Добавь курсы и сертификаты, которые могут усилить резюме и сопроводительные письма.",
      "add": "Добавить курс",
      "edit": "Изменить",
      "delete": "Удалить",
      "save": "Сохранить",
      "cancel": "Отмена",
      "addTitle": "Добавить курс",
      "editTitle": "Изменить курс",
      "detailsTitle": "Детали курса",
      "courseName": "Название курса / сертификата",
      "provider": "Провайдер / организация",
      "startDate": "Дата начала",
      "endDate": "Дата окончания",
      "credentialUrl": "Ссылка на сертификат",
      "skills": "Навыки / темы",
      "description": "Краткое описание",
      "courseColumn": "Курс",
      "providerColumn": "Провайдер",
      "skillsColumn": "Навыки",
      "searchPlaceholder": "Поиск курсов, провайдеров, навыков",
      "dateFrom": "С",
      "dateTo": "По",
      "emptyTitle": "Пока нет записей",
      "emptyHint": "Курсы и сертификаты пока не добавлены. Добавь записи об обучении, которые могут пригодиться для будущих резюме.",
      "noRecordsStatus": "Пока нет записей",
      "rowsPerPage": "Строк на странице",
      "pageReport": "Показано {first}–{last} из {totalRecords}",
      "resetFilters": "Сброс",
      "dateFilterError": "Дата окончания должна быть позже даты начала",
      "searchMinChars": "Введи минимум 3 символа для поиска"
    },
    "additional": {
      "title": "Дополнительная информация",
      "save": "Сохранить",
      "block1Title": "Настройки резюме и публичного профиля",
      "block2Title": "Предпочтения по работе",
      "block3Title": "Профессиональная информация",
      "block4Title": "Личная информация",
      "username": "Имя пользователя",
      "usernamePlaceholder": "yourname",
      "usernameHelp": "Это имя будет использоваться как часть ссылки на созданное резюме, например: resumainer.com/yourusername/YRFJ",
      "usernameInvalid": "Имя должно содержать только латинские буквы, цифры, дефис и подчёркивание.",
      "defaultResumeLanguage": "Основной язык резюме",
      "additionalResumeLanguage": "Дополнительный язык резюме",
      "languageEnglish": "Английский",
      "languageRussian": "Русский",
      "acceptableWorkFormats": "Подходящие форматы работы",
      "willingnessToRelocate": "Готовность к переезду",
      "willingnessForBusinessTravel": "Готовность к командировкам",
      "office": "Офис",
      "remote": "Удалённо",
      "hybrid": "Гибрид",
      "relocation": "Переезд",
      "rotationalSchedule": "Вахтовый график",
      "yes": "Да",
      "no": "Нет",
      "negotiable": "Обсуждаемо",
      "skills": "Навыки",
      "skillsPlaceholder": "Пример: бизнес-анализ, коммуникация со стейкхолдерами, SQL, Excel, моделирование процессов",
      "spokenLanguages": "Языки",
      "spokenLanguagesPlaceholder": "Английский C1, русский родной",
      "professionalAspirations": "Профессиональные стремления",
      "professionalAspirationsPlaceholder": "Пример: хочу развиваться в роли backend-разработчика и работать над продуктами, которые решают реальные бизнес-задачи.",
      "achievements": "Достижения",
      "achievementsPlaceholder": "Пример: улучшил процесс отчётности, подготовил требования для 50+ бизнес-кейсов, автоматизировал рутинные задачи анализа.",
      "additionalContextForAI": "Дополнительный контекст для AI",
      "additionalContextForAIPlaceholder": "Пример: предпочитаю лаконичные резюме, хочу подчеркнуть аналитическое мышление и не хочу преувеличивать опыт.",
      "additionalContextHint": "Добавь рабочий контекст, который поможет AI адаптировать резюме. Не добавляй личную информацию, которую не нужно показывать в резюме.",
      "dateOfBirth": "Дата рождения",
      "citizenship": "Гражданство"
    },
    "deleteConfirm": {
      "title": "Удалить запись?",
      "message": "Это действие нельзя отменить.",
      "cancel": "Отмена",
      "confirm": "Удалить"
    }
  },
  "common": {
    "loading": "Загрузка..."
  }
}

```

`src\main.ts`:

```ts
import { createApp } from 'vue'
import PrimeVue from 'primevue/config'
import Aura from '@primeuix/themes/aura'
import ToastService from 'primevue/toastservice'
import ConfirmationService from 'primevue/confirmationservice'
import Tooltip from 'primevue/tooltip'
import router from './router'
import i18n from './i18n'
import App from './App.vue'

import 'primeicons/primeicons.css'
import './assets/styles/vue_general.css'

const app = createApp(App)

app.use(PrimeVue, {
  theme: {
    preset: Aura,
    options: {
      prefix: 'p',
      darkModeSelector: '.p-dark',
      cssLayer: false
    }
  }
})

app.use(ToastService)
app.use(ConfirmationService)
app.directive('tooltip', Tooltip)
app.use(router)
app.use(i18n)

app.mount('#app')

```

`src\router\index.ts`:

```ts
import { createRouter, createWebHistory } from 'vue-router'
import { checkAuthStatus } from '@/services/authService'
import GeneratePlaceholderPage from '@/components/common/GeneratePlaceholderPage.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/auth',
      name: 'auth',
      component: () => import('@/views/AuthPage.vue'),
      meta: { requiresGuest: true }
    },
    {
      path: '/home',
      name: 'user-home',
      component: () => import('@/views/UserHomePage.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/admin',
      name: 'admin-home',
      component: () => import('@/views/AdminHomePage.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    // Profile sections
    {
      path: '/profile',
      redirect: '/profile/contact'
    },
    {
      path: '/profile/contact',
      name: 'profile-contact',
      component: () => import('@/views/ProfilePage.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/profile/experience',
      name: 'profile-experience',
      component: () => import('@/views/ProfilePage.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/profile/education',
      name: 'profile-education',
      component: () => import('@/views/ProfilePage.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/profile/projects',
      name: 'profile-projects',
      component: () => import('@/views/ProfilePage.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/profile/courses',
      name: 'profile-courses',
      component: () => import('@/views/ProfilePage.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/profile/additional',
      name: 'profile-additional',
      component: () => import('@/views/ProfilePage.vue'),
      meta: { requiresAuth: true }
    },
    // Generate resume placeholders (with stepper)
    {
      path: '/generate/vacancy',
      name: 'generate-vacancy',
      component: GeneratePlaceholderPage,
      meta: { requiresAuth: true }
    },
    {
      path: '/generate/settings',
      name: 'generate-settings',
      component: GeneratePlaceholderPage,
      meta: { requiresAuth: true }
    },
    {
      path: '/generate/review',
      name: 'generate-review',
      component: GeneratePlaceholderPage,
      meta: { requiresAuth: true }
    },
    {
      path: '/generate/export',
      name: 'generate-export',
      component: GeneratePlaceholderPage,
      meta: { requiresAuth: true }
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/auth'
    }
  ]
})

/**
 * Global navigation guard.
 *
 * - Protected routes (requiresAuth): redirect to /app/auth if not authenticated.
 * - Auth routes (requiresGuest): redirect to /app/home if already authenticated.
 * - Admin routes (requiresAdmin): redirect to /app/home if role is not ADMIN.
 */
router.beforeEach(async (to, from, next) => {
  try {
    const status = await checkAuthStatus()
    const isAuthenticated = status.authenticated
    const role = status.role

    // Route requires auth but user is not authenticated
    if (to.matched.some(r => r.meta.requiresAuth) && !isAuthenticated) {
      next({ path: '/auth', query: { redirect: to.fullPath } })
      return
    }

    // Route is for guests only but user is authenticated
    if (to.matched.some(r => r.meta.requiresGuest) && isAuthenticated) {
      next({ path: '/home' })
      return
    }

    // Route requires ADMIN role but user is not admin
    if (to.matched.some(r => r.meta.requiresAdmin) && role !== 'ADMIN') {
      next({ path: '/home' })
      return
    }

    next()
  } catch {
    // Auth check failed (network error, etc.) — treat as not authenticated
    if (to.matched.some(r => r.meta.requiresAuth)) {
      next({ path: '/auth' })
    } else {
      next()
    }
  }
})

export default router

```

`src\services\authService.ts`:

```ts
export interface AuthStatus {
  authenticated: boolean
  role: string | null
}

const mockAuth: AuthStatus = { authenticated: true, role: 'USER' }

export async function checkAuthStatus(): Promise<AuthStatus> {
  return mockAuth
}

export async function login(email: string, password: string): Promise<void> {
  mockAuth.authenticated = true
  mockAuth.role = 'USER'
}

export async function register(email: string, password: string): Promise<void> {
  mockAuth.authenticated = true
  mockAuth.role = 'USER'
}

export async function logout(): Promise<void> {
  mockAuth.authenticated = false
  mockAuth.role = null
}

```

`src\services\profileMockService.ts`:

```ts
import type { ContactDetails, WorkExperience, Project, Education, Course, AdditionalInfo, ProfileData } from '@/types/profile'

const STORAGE_KEY = 'resumainer_profile_data'

function generateId(): string {
  return Date.now().toString(36) + Math.random().toString(36).substring(2, 6)
}

function getDefaultProfile(): ProfileData {
  return {
    contactDetails: {
      fullName: '',
      professionalTitle: '',
      email: '',
      phone: '',
      location: '',
      linkedinUrl: '',
      portfolioUrl: '',
      telegram: '',
      whatsapp: ''
    },
    workExperience: [],
    projects: [],
    education: [],
    courses: [],
    additionalInfo: {
      username: '',
      defaultResumeLanguage: '',
      additionalResumeLanguage: '',
      acceptableWorkFormats: [],
      willingnessToRelocate: '',
      willingnessForBusinessTravel: '',
      skills: '',
      spokenLanguages: '',
      professionalAspirations: '',
      achievements: '',
      additionalContextForAI: '',
      dateOfBirth: '',
      citizenship: ''
    }
  }
}

function loadProfile(): ProfileData {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) {
      const data = JSON.parse(raw) as ProfileData
      return data
    }
  } catch { /* ignore */ }
  return getDefaultProfile()
}

function saveProfile(data: ProfileData): void {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(data))
}

// --- Contact Details ---

export function getContactDetails(): ContactDetails {
  return loadProfile().contactDetails
}

export function saveContactDetails(data: ContactDetails): void {
  const profile = loadProfile()
  profile.contactDetails = data
  saveProfile(profile)
}

// --- Work Experience ---

export function getWorkExperience(): WorkExperience[] {
  return loadProfile().workExperience
}

export function saveWorkExperienceRecord(record: WorkExperience): WorkExperience[] {
  const profile = loadProfile()
  const idx = profile.workExperience.findIndex(e => e.id === record.id)
  if (idx >= 0) {
    profile.workExperience[idx] = record
  } else {
    record.id = generateId()
    profile.workExperience.push(record)
  }
  saveProfile(profile)
  return profile.workExperience
}

export function deleteWorkExperienceRecord(id: string): WorkExperience[] {
  const profile = loadProfile()
  profile.workExperience = profile.workExperience.filter(e => e.id !== id)
  saveProfile(profile)
  return profile.workExperience
}

// --- Projects ---

export function getProjects(): Project[] {
  return loadProfile().projects
}

export function saveProjectRecord(record: Project): Project[] {
  const profile = loadProfile()
  const idx = profile.projects.findIndex(e => e.id === record.id)
  if (idx >= 0) {
    profile.projects[idx] = record
  } else {
    record.id = generateId()
    profile.projects.push(record)
  }
  saveProfile(profile)
  return profile.projects
}

export function deleteProjectRecord(id: string): Project[] {
  const profile = loadProfile()
  profile.projects = profile.projects.filter(e => e.id !== id)
  saveProfile(profile)
  return profile.projects
}

// --- Education ---

export function getEducation(): Education[] {
  return loadProfile().education
}

export function saveEducationRecord(record: Education): Education[] {
  const profile = loadProfile()
  const idx = profile.education.findIndex(e => e.id === record.id)
  if (idx >= 0) {
    profile.education[idx] = record
  } else {
    record.id = generateId()
    profile.education.push(record)
  }
  saveProfile(profile)
  return profile.education
}

export function deleteEducationRecord(id: string): Education[] {
  const profile = loadProfile()
  profile.education = profile.education.filter(e => e.id !== id)
  saveProfile(profile)
  return profile.education
}

// --- Courses ---

export function getCourses(): Course[] {
  return loadProfile().courses
}

export function saveCourse(record: Course): Course[] {
  const profile = loadProfile()
  const idx = profile.courses.findIndex(e => e.id === record.id)
  if (idx >= 0) {
    profile.courses[idx] = record
  } else {
    record.id = generateId()
    profile.courses.push(record)
  }
  saveProfile(profile)
  return profile.courses
}

export function deleteCourse(id: string): Course[] {
  const profile = loadProfile()
  profile.courses = profile.courses.filter(e => e.id !== id)
  saveProfile(profile)
  return profile.courses
}

// --- Additional Info ---

export function getAdditionalInfo(): AdditionalInfo {
  return loadProfile().additionalInfo
}

export function saveAdditionalInfo(data: AdditionalInfo): void {
  const profile = loadProfile()
  profile.additionalInfo = data
  saveProfile(profile)
}

// --- Section Status ---

export function getProfileSectionStatuses() {
  const profile = loadProfile()
  const cd = profile.contactDetails
  const contactComplete = !!(cd.fullName && cd.professionalTitle && cd.email && cd.phone && cd.location)

  const ai = profile.additionalInfo
  const additionalComplete = !!(ai.username && ai.defaultResumeLanguage && ai.additionalResumeLanguage)

  return {
    contactComplete,
    experienceCount: profile.workExperience.length,
    projectsCount: profile.projects.length,
    educationCount: profile.education.length,
    coursesCount: profile.courses.length,
    additionalComplete
  }
}

// --- Seed sample data ---

export function seedSampleData(): void {
  const profile = loadProfile()
  if (profile.workExperience.length > 0 || profile.education.length > 0) return

  profile.contactDetails = {
    fullName: 'John Doe',
    professionalTitle: 'Business Analyst, Junior Java Developer',
    email: 'johndoe@example.com',
    phone: '+7-777-777-77-77',
    location: 'Kazakhstan, Astana',
    linkedinUrl: 'https://www.linkedin.com/in/john/',
    portfolioUrl: '',
    telegram: '@johndoe',
    whatsapp: ''
  }

  profile.workExperience = [
    {
      id: 'we1',
      jobTitle: 'Junior Business Analyst',
      companyName: 'Tech Solutions Ltd',
      location: 'Astana, Kazakhstan',
      startDate: '2024-01-15',
      endDate: '',
      currentlyWorkHere: true,
      description: 'Gathering and documenting business requirements. Creating user stories, process flows, and wireframes. Collaborating with development team on feature implementation.',
      companyUrl: 'https://techsolutions.example.com'
    },
    {
      id: 'we2',
      jobTitle: 'Intern Java Developer',
      companyName: 'Digital Innovations Inc',
      location: 'Astana, Kazakhstan',
      startDate: '2023-06-01',
      endDate: '2023-12-31',
      currentlyWorkHere: false,
      description: 'Assisted in developing REST APIs using Spring Boot. Wrote unit tests with JUnit. Participated in code reviews and agile ceremonies.',
      companyUrl: ''
    }
  ]

  profile.education = [
    {
      id: 'ed1',
      institutionName: 'Astana IT University',
      degree: 'Bachelor of Science',
      fieldOfStudy: 'Computer Science',
      startDate: '2021-09-01',
      endDate: '',
      currentlyStudying: true,
      location: 'Astana, Kazakhstan',
      comment: 'Focus on software engineering and data analysis.',
      gpa: '3.8'
    }
  ]

  profile.courses = [
    { id: 'co1', courseName: 'Java Programming Masterclass', provider: 'Udemy', startDate: '2023-03-01', endDate: '2023-06-30', credentialUrl: '', skills: 'Java, Spring, Hibernate', description: 'Comprehensive Java course' },
    { id: 'co2', courseName: 'SQL for Data Analysis', provider: 'Coursera', startDate: '2023-07-01', endDate: '2023-09-30', credentialUrl: '', skills: 'SQL, PostgreSQL, Data Modeling', description: '' },
    { id: 'co3', courseName: 'Business Analysis Fundamentals', provider: 'LinkedIn Learning', startDate: '2024-02-01', endDate: '2024-04-30', credentialUrl: '', skills: 'Requirements, UML, Agile', description: '' }
  ]

  profile.additionalInfo = {
    username: 'johndoe',
    defaultResumeLanguage: 'en',
    additionalResumeLanguage: 'ru',
    acceptableWorkFormats: ['remote', 'hybrid'],
    willingnessToRelocate: 'negotiable',
    willingnessForBusinessTravel: 'yes',
    skills: 'Business Analysis, Java, Spring, SQL, UML, Agile, REST APIs, Requirements Gathering',
    spokenLanguages: 'English C1, Russian native',
    professionalAspirations: 'To become a senior business analyst with deep technical understanding.',
    achievements: 'Successfully delivered 3 major projects ahead of schedule.',
    additionalContextForAI: 'Prefer clean structured resumes with quantifiable achievements.',
    dateOfBirth: '',
    citizenship: 'Kazakhstan'
  }

  saveProfile(profile)
}

```

`src\services\resumeService.ts`:

```ts
/**
 * Resume API service.
 *
 * Fetches paginated saved resumes from GET /api/resumes
 * and soft-deletes via DELETE /api/resumes/{id}.
 */
import type { SavedResumeData } from './userHomeService'

export interface PagedResponse {
  items: SavedResumeData[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface ResumeQueryParams {
  search?: string
  language?: string
  adaptationLevel?: string
  createdDate?: string
  dateFrom?: string
  dateTo?: string
  sort?: string
  page?: number
  size?: number
}

export async function fetchResumes(params: ResumeQueryParams = {}): Promise<PagedResponse> {
  const query = new URLSearchParams()
  if (params.search) query.set('search', params.search)
  if (params.language) query.set('language', params.language)
  if (params.adaptationLevel) query.set('adaptationLevel', params.adaptationLevel)
  if (params.createdDate) query.set('createdDate', params.createdDate)
  if (params.dateFrom) query.set('dateFrom', params.dateFrom)
  if (params.dateTo) query.set('dateTo', params.dateTo)
  if (params.sort) query.set('sort', params.sort)
  if (params.page !== undefined) query.set('page', String(params.page))
  if (params.size !== undefined) query.set('size', String(params.size))

  const qs = query.toString()
  const url = `/api/resumes${qs ? '?' + qs : ''}`

  const res = await fetch(url, { credentials: 'include' })
  if (!res.ok) throw new Error('Failed to fetch resumes')
  return res.json()
}

export async function deleteResume(id: number): Promise<void> {
  const res = await fetch(`/api/resumes/${id}`, {
    method: 'DELETE',
    credentials: 'include'
  })
  if (!res.ok) throw new Error('Failed to delete resume')
}

```

`src\services\userHomeService.ts`:

```ts
/**
 * User Home API service.
 *
 * Fetches profile readiness summary from GET /api/user/home.
 */

export interface ProfileChecklist {
  contactDetails: boolean
  workExperience: boolean
  education: boolean
}

export interface HomeSummary {
  savedResumesCount: number
  profileStatus: string
  lastResumeId: number | null
}

export interface SavedResumeData {
  id: number
  resumeTitle: string
  vacancy: string
  company: string
  language: string
  adaptationLevel: string
  createdAt: string
  publicUrl: string
  pdfUrl: string
  coverLetter: string | null
}

export interface UserHomeSummary {
  profileReady: boolean
  profileChecklist: ProfileChecklist
  summary: HomeSummary
  lastResume: SavedResumeData | null
}

export async function fetchSummary(): Promise<UserHomeSummary> {
  const res = await fetch('/api/user/home', { credentials: 'include' })
  if (!res.ok) throw new Error('Failed to fetch home summary')
  return res.json()
}

```

`src\types\profile.ts`:

```ts
export interface ContactDetails {
  fullName: string
  professionalTitle: string
  email: string
  phone: string
  location: string
  linkedinUrl: string
  portfolioUrl: string
  telegram: string
  whatsapp: string
}

export interface WorkExperience {
  id: string
  jobTitle: string
  companyName: string
  location: string
  startDate: string
  endDate: string
  currentlyWorkHere: boolean
  description: string
  companyUrl: string
}

export interface Project {
  id: string
  projectName: string
  role: string
  startDate: string
  endDate: string
  isOngoing: boolean
  description: string
  projectUrl: string
}

export interface Education {
  id: string
  institutionName: string
  degree: string
  fieldOfStudy: string
  startDate: string
  endDate: string
  currentlyStudying: boolean
  location: string
  comment: string
  gpa: string
}

export interface Course {
  id: string
  courseName: string
  provider: string
  startDate: string
  endDate: string
  credentialUrl: string
  skills: string
  description: string
}

export interface AdditionalInfo {
  username: string
  defaultResumeLanguage: string
  additionalResumeLanguage: string
  acceptableWorkFormats: string[]
  willingnessToRelocate: string
  willingnessForBusinessTravel: string
  skills: string
  spokenLanguages: string
  professionalAspirations: string
  achievements: string
  additionalContextForAI: string
  dateOfBirth: string
  citizenship: string
}

export interface ProfileData {
  contactDetails: ContactDetails
  workExperience: WorkExperience[]
  projects: Project[]
  education: Education[]
  courses: Course[]
  additionalInfo: AdditionalInfo
}

export interface SectionStatus {
  key: string
  label: string
  route: string
  statusText: string
  statusType: 'completed' | 'incomplete' | 'count' | 'no-records' | 'empty'
}

```

`src\views\AdminHomePage.vue`:

```vue
<template>
  <div class="page">
    <AppHeader />
    <main class="main-content">
      <h1>Admin Home</h1>
      <p>Placeholder - under construction</p>
    </main>
  </div>
</template>

<script setup lang="ts">
import AppHeader from '@/components/AppHeader.vue'
</script>

<style scoped>
.page { display: flex; flex-direction: column; min-height: 100vh; background: #F6F7FB; }
.main-content { max-width: 1280px; width: 100%; margin: 0 auto; padding: 36px 32px; }
</style>

```

`src\views\AuthPage.vue`:

```vue
<template>
  <div class="page">
    <AppHeader />
    <main class="main-content">
      <h1>Auth Page</h1>
      <p>Placeholder - under construction</p>
    </main>
  </div>
</template>

<script setup lang="ts">
import AppHeader from '@/components/AppHeader.vue'
</script>

<style scoped>
.page { display: flex; flex-direction: column; min-height: 100vh; background: #F6F7FB; }
.main-content { max-width: 1280px; width: 100%; margin: 0 auto; padding: 36px 32px; }
</style>

```

`src\views\ProfilePage.vue`:

```vue
<template>
  <div class="page">
    <AppHeader />
    <ProfileShell :sections="sectionStatuses" :activeSection="currentSection">
      <ContactDetailsSection
        v-if="currentSection === 'contact'"
        ref="contactRef"
        @saved="refreshStatuses"
        @dirty-change="onDirtyChange('contact', $event)"
      />
      <WorkExperienceSection
        v-else-if="currentSection === 'experience'"
        ref="experienceRef"
        @saved="refreshStatuses"
        @dirty-change="onDirtyChange('experience', $event)"
      />
      <ProjectsSection
        v-else-if="currentSection === 'projects'"
        ref="projectsRef"
        @saved="refreshStatuses"
        @dirty-change="onDirtyChange('projects', $event)"
      />
      <EducationSection
        v-else-if="currentSection === 'education'"
        ref="educationRef"
        @saved="refreshStatuses"
        @dirty-change="onDirtyChange('education', $event)"
      />
      <CoursesSection
        v-else-if="currentSection === 'courses'"
        ref="coursesRef"
        @saved="refreshStatuses"
        @dirty-change="onDirtyChange('courses', $event)"
      />
      <AdditionalInfoSection
        v-else-if="currentSection === 'additional'"
        ref="additionalRef"
        @saved="refreshStatuses"
        @dirty-change="onDirtyChange('additional', $event)"
      />
    </ProfileShell>
    <UnsavedChangesDialog
      v-model:visible="unsavedDialogVisible"
      @confirm-leave="confirmLeave"
      @cancel-stay="cancelStay"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter, onBeforeRouteLeave, onBeforeRouteUpdate } from 'vue-router'
import { useI18n } from 'vue-i18n'
import AppHeader from '@/components/AppHeader.vue'
import ProfileShell from '@/components/profile/ProfileShell.vue'
import ContactDetailsSection from '@/components/profile/sections/ContactDetailsSection.vue'
import WorkExperienceSection from '@/components/profile/sections/WorkExperienceSection.vue'
import ProjectsSection from '@/components/profile/sections/ProjectsSection.vue'
import EducationSection from '@/components/profile/sections/EducationSection.vue'
import CoursesSection from '@/components/profile/sections/CoursesSection.vue'
import AdditionalInfoSection from '@/components/profile/sections/AdditionalInfoSection.vue'
import UnsavedChangesDialog from '@/components/profile/UnsavedChangesDialog.vue'
import { getProfileSectionStatuses, seedSampleData } from '@/services/profileMockService'
import type { SectionStatus } from '@/types/profile'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()

const contactRef = ref<InstanceType<typeof ContactDetailsSection> | null>(null)
const experienceRef = ref<InstanceType<typeof WorkExperienceSection> | null>(null)
const projectsRef = ref<InstanceType<typeof ProjectsSection> | null>(null)
const educationRef = ref<InstanceType<typeof EducationSection> | null>(null)
const coursesRef = ref<InstanceType<typeof CoursesSection> | null>(null)
const additionalRef = ref<InstanceType<typeof AdditionalInfoSection> | null>(null)

const dirtyMap = ref<Record<string, boolean>>({})
const unsavedDialogVisible = ref(false)
const pendingRoute = ref<string | null>(null)

const currentSection = computed(() => {
  const path = route.path
  if (path.includes('/contact')) return 'contact'
  if (path.includes('/experience')) return 'experience'
  if (path.includes('/projects')) return 'projects'
  if (path.includes('/education')) return 'education'
  if (path.includes('/courses')) return 'courses'
  if (path.includes('/additional')) return 'additional'
  return 'contact'
})

const sectionStatuses = computed<SectionStatus[]>(() => {
  const statuses = getProfileSectionStatuses()
  return [
    {
      key: 'contact',
      label: t('profile.subnav.contact'),
      route: '/profile/contact',
      statusText: statuses.contactComplete ? t('profile.status.completed') : t('profile.status.incomplete'),
      statusType: statuses.contactComplete ? 'completed' : 'incomplete'
    },
    {
      key: 'experience',
      label: t('profile.subnav.experience'),
      route: '/profile/experience',
      statusText: formatCount(statuses.experienceCount),
      statusType: statuses.experienceCount > 0 ? 'count' : 'no-records'
    },
    {
      key: 'projects',
      label: t('profile.subnav.projects'),
      route: '/profile/projects',
      statusText: statuses.projectsCount > 0 ? formatCount(statuses.projectsCount) : t('profile.status.noRecords'),
      statusType: statuses.projectsCount > 0 ? 'count' : 'no-records'
    },
    {
      key: 'education',
      label: t('profile.subnav.education'),
      route: '/profile/education',
      statusText: formatCount(statuses.educationCount),
      statusType: statuses.educationCount > 0 ? 'count' : 'no-records'
    },
    {
      key: 'courses',
      label: t('profile.subnav.courses'),
      route: '/profile/courses',
      statusText: statuses.coursesCount > 0 ? formatCount(statuses.coursesCount) : t('profile.status.noRecords'),
      statusType: statuses.coursesCount > 0 ? 'count' : 'no-records'
    },
    {
      key: 'additional',
      label: t('profile.subnav.additional'),
      route: '/profile/additional',
      statusText: statuses.additionalComplete ? t('profile.status.completed') : t('profile.status.incomplete'),
      statusType: statuses.additionalComplete ? 'completed' : 'incomplete'
    }
  ]
})

function formatCount(count: number): string {
  if (count === 1) return t('profile.status.record', { count })
  if (count === 0) return t('profile.status.noRecordsShort')
  return t('profile.status.records', { count })
}

function onDirtyChange(section: string, dirty: boolean) {
  dirtyMap.value[section] = dirty
}

function hasUnsavedChanges(): boolean {
  return Object.values(dirtyMap.value).some(v => v === true)
}

function refreshStatuses() {
  // reactivity handles computed recompute
}

// Navigation guards for unsaved changes
onBeforeRouteLeave((to, from, next) => {
  if (hasUnsavedChanges()) {
    unsavedDialogVisible.value = true
    pendingRoute.value = to.fullPath
    next(false)
  } else {
    next()
  }
})

onBeforeRouteUpdate((to, from, next) => {
  if (hasUnsavedChanges()) {
    unsavedDialogVisible.value = true
    pendingRoute.value = to.fullPath
    next(false)
  } else {
    next()
  }
})

function confirmLeave() {
  dirtyMap.value = {}
  if (pendingRoute.value) {
    router.push(pendingRoute.value)
    pendingRoute.value = null
  }
}

function cancelStay() {
  pendingRoute.value = null
}

// Seed sample data on first visit
onMounted(() => {
  seedSampleData()
})

// Guard against browser refresh
window.addEventListener('beforeunload', (e) => {
  if (hasUnsavedChanges()) {
    e.preventDefault()
  }
})
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: var(--vue-bg-canvas);
}
</style>

```

`src\views\UserHomePage.vue`:

```vue
<template>
  <div class="page">
    <AppHeader />

    <main class="page-main">
      <h1 class="page-h1">{{ $t('home.title') }}</h1>

      <!-- Guided block: loading / error / content -->
      <div v-if="summaryLoading" class="skeleton-block">
        <Skeleton width="60%" height="24px" />
        <Skeleton width="100%" height="80px" style="margin-top: 1rem;" />
      </div>
      <div v-else-if="summaryError" class="inline-error">
        <i class="pi pi-exclamation-triangle" style="color: #D97706; font-size: 1.25rem;"></i>
        <span>{{ summaryError }}</span>
        <Button icon="pi pi-refresh" label="Retry" class="p-button-text p-button-sm" @click="loadSummary" />
      </div>
      <GuidedNextStep
        v-else-if="summary"
        :profileReady="summary.profileReady"
        :checklist="summary.profileChecklist"
      />

      <!-- Summary cards -->
      <div v-if="summaryLoading" class="skeleton-block">
        <div class="summary-skeleton-row">
          <Skeleton width="100%" height="100px" v-for="i in 3" :key="i" />
        </div>
      </div>
      <SummaryCards
        v-else-if="summary"
        :savedResumesCount="summary.summary.savedResumesCount"
        :profileReady="summary.profileReady"
        :lastResume="summary.lastResume"
        @openLastResume="openLastResume"
      />

      <!-- Saved Resumes section -->
      <div class="section-header">
        <h2>{{ $t('home.table.title') }}</h2>
        <Button
          v-if="summary?.profileReady"
          :label="$t('home.ready.generate.cta')"
          icon="pi pi-plus"
          class="p-button-success p-button-outlined"
          v-tooltip.top="$t('home.ready.generate.tooltip')"
          @click="$router.push('/generate/vacancy')"
        />
      </div>

      <!-- Table: loading / error / content -->
      <div v-if="tableLoading && resumes.length === 0" class="skeleton-block">
        <Skeleton width="100%" height="200px" />
      </div>
      <div v-else-if="tableError" class="inline-error">
        <i class="pi pi-exclamation-triangle" style="color: #C2410C; font-size: 1.25rem;"></i>
        <span>{{ tableError }}</span>
        <Button icon="pi pi-refresh" label="Retry" class="p-button-text p-button-sm" @click="loadResumes" />
      </div>
      <SavedResumesTable
        v-else
        :resumes="resumes"
        :totalRecords="totalRecords"
        :loading="tableLoading"
        :first="firstRow"
        :sortField="currentSortField"
        :sortOrder="currentSortOrder"
        :size="queryParams.size || 10"
        @page="onPage"
        @sort="onSort"
        @filter="onFilter"
        @search="onSearch"
        @openResume="openResumeModal"
      />

      <!-- Resume Details Modal -->
      <ResumeDetailsDialog
        v-model:visible="modalVisible"
        :resume="selectedResume"
        @delete="handleDelete"
      />
    </main>
  </div>
</template>

<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserHome } from '@/composables/useUserHome'
import AppHeader from '@/components/AppHeader.vue'
import GuidedNextStep from '@/components/home/GuidedNextStep.vue'
import SummaryCards from '@/components/home/SummaryCards.vue'
import SavedResumesTable from '@/components/home/SavedResumesTable.vue'
import ResumeDetailsDialog from '@/components/home/ResumeDetailsDialog.vue'
import Button from 'primevue/button'
import Skeleton from 'primevue/skeleton'

const router = useRouter()

const {
  summary,
  summaryLoading,
  summaryError,
  resumes,
  totalRecords,
  tableLoading,
  tableError,
  queryParams,
  selectedResume,
  modalVisible,
  fetchAll,
  loadSummary,
  loadResumes,
  onPage,
  onSort,
  onFilter,
  onSearch,
  openResumeModal,
  closeModal,
  handleDelete
} = useUserHome()

const firstRow = computed(() => (queryParams.page || 0) * (queryParams.size || 10))

const currentSortField = computed(() => {
  const sort = queryParams.sort || 'createdAt,desc'
  return sort.split(',')[0] || 'createdAt'
})

const currentSortOrder = computed(() => {
  const sort = queryParams.sort || 'createdAt,desc'
  return sort.split(',')[1] === 'asc' ? 1 : -1
})

function openLastResume() {
  if (summary.value?.lastResume) {
    openResumeModal(summary.value.lastResume)
  }
}

onMounted(() => {
  fetchAll()
})
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #F6F7FB;
}
.page-main {
  flex: 1;
  max-width: 1280px;
  width: 100%;
  margin: 0 auto;
  padding: 1.5rem 1.5rem 2rem;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}
.page-h1 {
  font-family: 'Manrope', sans-serif;
  font-size: 1.5rem;
  font-weight: 700;
  color: #10233F;
  margin: 0;
}
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 0.5rem;
}
.section-header h2 {
  font-family: 'Manrope', sans-serif;
  font-size: 1.15rem;
  font-weight: 700;
  color: #10233F;
  margin: 0;
}
.skeleton-block {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}
.summary-skeleton-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
}
.inline-error {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem;
  background: #FFF7ED;
  border: 1px solid #FDE68A;
  border-radius: 8px;
  color: #92400E;
  font-size: 0.9rem;
}
@media (max-width: 639px) {
  .summary-skeleton-row {
    grid-template-columns: 1fr;
  }
}
</style>

```

`tsconfig.json`:

```json
{
  "files": [],
  "references": [
    { "path": "./tsconfig.app.json" },
    { "path": "./tsconfig.node.json" }
  ]
}

```

`tsconfig.node.json`:

```json
{
  "compilerOptions": {
    "target": "ES2022",
    "lib": ["ES2023"],
    "module": "ESNext",
    "skipLibCheck": true,
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "isolatedModules": true,
    "moduleDetection": "force",
    "noEmit": true,
    "strict": true,
    "noUnusedLocals": false,
    "noUnusedParameters": false,
    "noFallthroughCasesInSwitch": true
  },
  "include": ["vite.config.ts"]
}

```

`vite.config.ts`:

```ts
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  }
})

```