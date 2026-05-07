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
