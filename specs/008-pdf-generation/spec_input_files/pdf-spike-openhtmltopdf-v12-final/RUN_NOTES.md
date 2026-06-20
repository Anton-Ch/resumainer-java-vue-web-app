# Run notes

1. Copy fonts to `src/main/resources/fonts` before PDF generation.
2. Delete old output before comparing runs:

```powershell
Remove-Item -Recurse -Force output-edge, work -ErrorAction SilentlyContinue
```

3. Run tests:

```powershell
mvn test
```

4. Run with debug attempts:

```powershell
mvn -q exec:java -Dexec.args="--mode=edge --out=output-edge --db=work/pdf-spike-edge.sqlite --debug-attempts=true"
```

5. Run without debug attempts to verify cleanup behavior:

```powershell
mvn -q exec:java -Dexec.args="--mode=edge --out=output-edge --db=work/pdf-spike-edge.sqlite --debug-attempts=false"
```

6. Inspect:
   - `output-edge/report/pdf-spike-report.md`
   - `output-edge/report/pdf-spike-report.json`
   - `output-edge/logs/pdf-spike.log`
   - per-scenario `output-edge/debug-attempts/...` only when debug attempts are enabled.

V12 notes:
- PDF and HTML should both include page navigation notes.
- `page2_delta_limit_percent` is now enforced for page2/page3 line-height and section gaps.
- Spike-only mock tables are documented as non-production in SQL comments and `TRANSFER_TO_MAIN_PROJECT.md`.


## Navigation note fix

Two-page and three-page outputs should show both directions consistently:

- page 1 footer: `See the next page` / `См. следующую страницу`;
- page 2/page 3 header: `See the previous page` / `См. предыдущую страницу`.

The renderer uses explicit `height:297mm` to make the absolute bottom footer visible in OpenHTMLToPDF as well as in the HTML artifact.
