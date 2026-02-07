# Afya Assistant – Web App

Single-page web app that mirrors the CHW visit workflow. Designed for:
- **Rural setting** – works offline (localStorage; sync is simulated).
- **High volume** – short flow: find patient → capture (voice or tap) → review suggestions → complete.
- **Low literacy / simple device** – big buttons, few screens, simple words.

Full workflow (context, steps, AI role, what’s stored, failure modes): see **docs/CHW_WORKFLOW.md**.

## How to run

**Option 1: Open the file**
- Double-click `index.html`, or  
- Drag it into a browser tab  

*Note: Voice input needs HTTPS or localhost in some browsers.*

**Option 2: Local server (recommended)**

```bash
# Python 3 (from project root)
cd webapp
python -m http.server 8081
```

Then open: **http://localhost:8081**

If port 8081 is blocked, use 5500: `python -m http.server 5500` → http://localhost:5500

**Option 3: Node**
```bash
npx serve webapp -p 8081
```

## What works

- **Find/Create patient** – Search by name, or create new (name, DOB, sex, village).
- **Capture** – Tap symptoms (Fever, Cough, Diarrhea, etc.) or use voice (Chrome; needs mic).
- **Danger sign** – If “Not eating” is added, you’re asked: “Can child drink?” Yes/No.
- **Suggestions** – Treatments (ORS, Zinc, Paracetamol) with reasons; Accept or Skip (with reason).
- **Summary** – Symptoms, treatments, follow-up (2 days / 5 days / none), then Complete.
- **Persistence** – Patients and visits are saved in localStorage and survive refresh.

## Data stored locally

- `afya_patients` – List of patients.
- `afya_visits` – List of completed visits (patient, date, symptoms, treatments, follow-up).
- `afya_pendingSync` – Count of “pending sync” (for UI only; no real sync in web app).

## Clear data

In the browser console (F12):

```javascript
localStorage.removeItem('afya_patients');
localStorage.removeItem('afya_visits');
localStorage.removeItem('afya_pendingSync');
location.reload();
```
