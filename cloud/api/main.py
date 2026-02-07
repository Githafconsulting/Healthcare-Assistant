"""
Afya Assistant API - Minimal backend for sync.
"""

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import Optional, List
from datetime import datetime

app = FastAPI(title="Afya Assistant API", version="1.0.0")


# ==================== Models ====================

class Patient(BaseModel):
    id: str
    name: str
    dateOfBirth: int
    sex: str
    village: str
    phone: Optional[str] = None
    caregiverName: Optional[str] = None
    createdAt: int
    updatedAt: int


class Visit(BaseModel):
    id: str
    patientId: str
    chwId: str
    startTime: int
    endTime: Optional[int] = None
    symptomsJson: str
    vitalsJson: Optional[str] = None
    dangerSignsJson: str
    assessment: Optional[str] = None
    treatment: Optional[str] = None
    referralJson: Optional[str] = None
    notes: Optional[str] = None


class AuditEntry(BaseModel):
    id: str
    timestamp: int
    chwId: str
    action: str
    entityType: str
    entityId: str
    details: Optional[str] = None


# ==================== Endpoints ====================

@app.get("/api/v1/health")
def health():
    return {"status": "ok", "timestamp": int(datetime.now().timestamp() * 1000)}


@app.post("/api/v1/patients")
def upload_patient(patient: Patient):
    # TODO: Save to database
    return {"success": True}


@app.post("/api/v1/visits")
def upload_visit(visit: Visit):
    # TODO: Save to database
    # If danger signs present, could trigger notification
    return {"success": True}


@app.post("/api/v1/audit/batch")
def upload_audit_batch(entries: List[AuditEntry]):
    # TODO: Bulk insert to database
    return {"success": True, "count": len(entries)}
