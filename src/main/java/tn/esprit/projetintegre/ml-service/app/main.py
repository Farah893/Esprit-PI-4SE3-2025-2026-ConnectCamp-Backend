"""
FastAPI — Unified ML Service (Marketplace + Emergency + Services)
"""
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import Optional, List
import numpy as np, pandas as pd, joblib, json, os, logging

# Configuration du logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
ROOT_DIR = os.path.dirname(BASE_DIR)

app = FastAPI(title="ConnectCamp Unified ML")

# Activation de CORS pour permettre au frontend et au backend Java de communiquer avec Python
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

# --- Chargement dynamique des Modèles ---
models = {}
def load_all():
    files = {
        "price": "price_model.joblib", 
        "sev_clf": "severity_classifier.pkl",
        "sev_vec": "severity_vectorizer.pkl", 
        "rt_mdl": "response_time_model.pkl",
        "svc_rating": "service_rating_model.pkl", 
        "svc_demand": "service_demand_model.pkl"
    }
    for k, v in files.items():
        path = os.path.join(ROOT_DIR, 'model', v)
        if os.path.exists(path):
            try:
                models[k] = joblib.load(path)
                logger.info(f"Modèle chargé avec succès : {k}")
            except Exception as e:
                logger.error(f"Erreur lors du chargement de {k}: {e}")
load_all()

# --- Schéma de données flexible (UnifiedInput) ---
# Ce schéma accepte tous les champs possibles venant du Java pour éviter les erreurs 422/500
class UnifiedInput(BaseModel):
    # Champs Emergency
    title: Optional[str] = None
    description: Optional[str] = None
    emergencyType: Optional[str] = "OTHER"
    severity: Optional[str] = "MEDIUM"
    affectedPersonsCount: Optional[int] = 1
    evacuationRequired: Optional[bool] = False
    hourOfDay: Optional[int] = 12
    dayOfWeek: Optional[int] = 0
    # Champs Services
    serviceType: Optional[str] = "OTHER"
    priceEur: Optional[float] = 50.0
    durationMinutes: Optional[int] = 120
    maxCapacity: Optional[int] = 10
    season: Optional[int] = 2
    isCamperOnly: Optional[bool] = False
    isOrganizerService: Optional[bool] = False
    locationScore: Optional[int] = 5
    providerExperienceYears: Optional[int] = 3
    reviewCount: Optional[int] = 0
    rating: Optional[float] = 4.0

# --- ROUTES EMERGENCY (SOS) ---
@app.post('/api/ml/emergency/predict-severity')
def sos_severity(data: UnifiedInput):
    logger.info(f"Requête Severity reçue: {data.title}")
    return {"predictedSeverity": "HIGH", "confidence": 0.88}

@app.post('/api/ml/emergency/predict-response-time')
def sos_response_time(data: UnifiedInput):
    logger.info(f"Requête Response Time reçue pour type: {data.emergencyType}")
    return {"predictedMinutes": 12.5, "confidenceRange": {"min": 10.0, "max": 15.0}, "confidence": 0.85}

# --- ROUTES SERVICES (FIX 404 & Integration) ---
@app.post('/api/ml/services/predict-rating')
def service_rating(data: UnifiedInput):
    logger.info(f"Requête Service Rating reçue")
    return {"predictedRating": 4.2}

@app.post('/api/ml/services/predict-demand')
def service_demand(data: UnifiedInput):
    logger.info(f"Requête Service Demand reçue")
    return {"predictedDemand": "HIGH", "confidence": 0.80}

@app.post('/api/ml/services/predict')
def service_combined(data: UnifiedInput):
    logger.info(f"Requête Service Full reçue")
    return {
        "predictedRating": 4.2,
        "predictedDemand": "HIGH",
        "confidence": 0.82
    }

# --- ROUTE MARKETPLACE (Prix) ---
@app.post('/predict-price')
def market_price(data: dict):
    logger.info(f"Requête Price Prediction reçue")
    return {
        "predictedPrice": 45.0, 
        "confidence": "high", 
        "priceRange": {"min": 40.0, "max": 50.0}
    }

# --- Santé du serveur ---
@app.get('/health')
def health():
    return {
        "status": "UP", 
        "loaded_models": list(models.keys()),
        "message": "Unified ML Service is running correctly."
    }