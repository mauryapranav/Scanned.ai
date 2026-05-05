from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from dotenv import load_dotenv
import google.generativeai as genai
import os

load_dotenv()
genai.configure(api_key=os.getenv("GEMINI_API_KEY"))
model = genai.GenerativeModel("gemini-2.5-flash")

app = FastAPI()

class ScanRequest(BaseModel):
    text: str
    mode: str  # "summarize" | "flashcards" | "exam_questions" | "ask"
    question: str = ""  # only used for "ask" mode

PROMPTS = {
    "summarize": lambda text, _: f"""You are an expert academic summarizer.
Summarize the following student notes concisely. Use bullet points.
Highlight the 3 most important concepts at the top.

NOTES:
{text}""",

    "flashcards": lambda text, _: f"""You are a study assistant.
Generate 8-10 flashcards from these notes.
Format EXACTLY as:
Q: [question]
A: [answer]

NOTES:
{text}""",

    "exam_questions": lambda text, _: f"""You are an exam paper setter.
Generate 5 likely exam questions from these notes.
Mix short answer and long answer types.
Label each: [Short] or [Long]

NOTES:
{text}""",

    "ask": lambda text, question: f"""You are a helpful tutor.
Answer the student's question using ONLY the context from their notes.
If the answer isn't in the notes, say so clearly.

NOTES:
{text}

QUESTION: {question}"""
}

@app.post("/process")
async def process(req: ScanRequest):
    if req.mode not in PROMPTS:
        raise HTTPException(status_code=400, detail=f"Invalid mode: {req.mode}")
    
    prompt = PROMPTS[req.mode](req.text, req.question)
    
    try:
        response = model.generate_content(prompt)
        return {"result": response.text}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/health")
def health():
    return {"status": "ok"}