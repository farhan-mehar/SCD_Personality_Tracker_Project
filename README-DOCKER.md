# 🐳 Psyche — Docker Setup Guide

Yeh guide aapko Psyche app ko Docker mein run karna sikhayegi — koi local MySQL ya Java setup ki zaroorat nahi, sab kuch containers mein chalega.

---

## 📋 Prerequisites

1. **Docker Desktop** install karo (Windows ke liye):
   👉 https://www.docker.com/products/docker-desktop/
2. Install karne ke baad Docker Desktop **open** karo aur wait karo jab tak "Engine running" green dikhe.

---

## 🚀 Step-by-Step: App Run Karna

### 1️⃣ `.env` file banao

`psyche` folder mein `.env.example` file hai. Usko copy karke naam `.env` rakho:

```powershell
copy .env.example .env
```

`.env` file open karo aur (optional) apna Gmail + App Password daal do email reminders ke liye:

```
MYSQL_ROOT_PASSWORD=rootpass
SPRING_MAIL_USERNAME=your_email@gmail.com
SPRING_MAIL_PASSWORD=your_16_char_app_password
```

> Agar email features test nahi karne (sirf app dekhna hai), to defaults rehne do — app phir bhi chalega, sirf emails fail honge (console mein warning aayegi, crash nahi hoga).

---

### 2️⃣ Build aur Run karo

`psyche` folder mein (jahan `Dockerfile` aur `docker-compose.yml` hain) terminal/PowerShell kholo:

```powershell
docker compose up --build
```

Pehli baar mein 2-5 minutes lagenge (Maven dependencies download + build). Aap dekhoge:
- `psyche-mysql` container start hoga
- `psyche-app` container build hoga aur start hoga

Jab yeh line dikhe:
```
Started PsycheApplication in X.XXX seconds
```
matlab app **ready** hai! ✅

---

### 3️⃣ Browser mein kholo

```
http://localhost:8080/login
```

Signup karo, login karo, quiz do — sab kaam karega!

---

## 🛑 App Band Karna

Terminal mein `Ctrl+C` dabao, phir:

```powershell
docker compose down
```

Data (MySQL database) **preserve** rahega — dobara `docker compose up` karne pe wahi data milega.

Agar database bhi **completely reset** karna ho (fresh start):

```powershell
docker compose down -v
```

---

## 🔧 Useful Commands

| Command | Kya karta hai |
|---------|---------------|
| `docker compose up --build` | Build + run (pehli baar ya code change ke baad) |
| `docker compose up` | Run (agar already build hai) |
| `docker compose up -d` | Background mein run (terminal free rehta hai) |
| `docker compose down` | Stop + remove containers (data safe) |
| `docker compose down -v` | Stop + remove containers + database data |
| `docker compose logs -f app` | App ke live logs dekho |
| `docker ps` | Running containers dekho |

---

## ❓ Troubleshooting

**Port already in use error?**
Agar `8080` ya `3307` already kisi aur program ne le liya hai, `docker-compose.yml` mein left side wala port number change kar do, e.g.:
```yaml
ports:
  - "8081:8080"   # ab http://localhost:8081 pe app chalega
```

**MySQL connection error?**
Pehli baar `mysql` container ko fully start hone mein ~20-30 seconds lagte hain. `depends_on: condition: service_healthy` is wajah se hai — `app` container automatically wait karega.

**Code change ke baad purana version chal raha hai?**
```powershell
docker compose up --build
```
`--build` flag zaroori hai taake naya image bane.
