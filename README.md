# Skanned.ai

An Android app that lets students scan any handwritten or printed notes/question papers and instantly get AI-powered summaries, flashcards, exam questions, or answers.

## Features

- **OCR** — Extract text from any image using ML Kit
- **Summarize** — Get concise bullet-point summaries of notes
- **Flashcards** — Auto-generate Q&A flashcards for revision
- **Exam Questions** — Predict likely exam questions from notes
- **Ask** — Ask any question about the scanned content
- **History** — View all past scans saved locally via Room DB
- **Secure** — Gemini API key never leaves the server

## Tech Stack

**Android**
- Kotlin + Jetpack Compose
- MVVM architecture
- ML Kit Text Recognition (OCR)
- Retrofit + OkHttp
- Room Database

**Backend**
- FastAPI (Python)
- Google Gemini 2.5 Flash
- Deployed on Render

## Architecture
[Android App]
│
├── ScanScreen → picks image → ML Kit OCR → extracted text
│
├── ScanViewModel (shared across all screens)
│        │
│        └── POST /process → FastAPI on Render
│                                │
│                                └── Gemini 2.5 Flash → result
│
├── ResultScreen → displays AI output
│
└── HistoryScreen → Room DB → past scans

## Setup

### Backend
```bash
git clone https://github.com/mauryapranav/Scanned.ai
cd backend
pip install -r requirements.txt

# Create .env file
echo "GEMINI_API_KEY=your_key_here" > .env

uvicorn main:app --reload
```

### Android
1. Open project in Android Studio
2. Change `BASE_URL` in `RetrofitInstance.kt` to your backend URL
3. Run on device or emulator

## API

`POST /process`
```json
{
  "text": "your extracted text",
  "mode": "summarize | flashcards | exam_questions | ask",
  "question": "only required for ask mode"
}
```

`GET /health` — returns `{"status": "ok"}`

## Screenshots
<!-- Add screenshots here -->

## Author
Pranav Maurya — [@mauryapranav](https://github.com/mauryapranav)
