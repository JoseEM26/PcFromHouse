import face_recognition
import cv2
import numpy as np
import sys

def distancia(p1, p2):
    return np.sqrt((p1[0] - p2[0])**2 + (p1[1] - p2[1])**2)

def analizar_forma_cara(landmarks):
    chin = landmarks['chin']
    left_eyebrow = landmarks['left_eyebrow']
    right_eyebrow = landmarks['right_eyebrow']
    
    # Puntos clave
    forehead_center = ((left_eyebrow[0][0] + right_eyebrow[2][0]) // 2, min(left_eyebrow[0][1], right_eyebrow[2][1]) - 30)
    chin_center = chin[8]
    face_width = distancia(chin[0], chin[16])
    face_height = distancia(forehead_center, chin_center)
    forehead_width = distancia(left_eyebrow[0], right_eyebrow[2])
    jaw_width = distancia(chin[4], chin[12])
    
    ratio = face_height / face_width if face_width > 0 else 1.0
    diff = (forehead_width - jaw_width) / face_width
    
    # Clasificación profesional
    if ratio > 1.45:
        return "Alargada", "LOW FADE"
    elif ratio < 1.05:
        if jaw_width > forehead_width * 0.95:
            return "Cuadrada", "HIGH FADE"
        else:
            return "Redonda", "HIGH FADE"
    elif abs(diff) > 0.12:
        if diff > 0:
            return "Corazón", "LOW FADE"
        else:
            return "Triángulo", "LOW FADE"
    else:
        return "Ovalada", "MID FADE"

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Uso: python barber_ia.py tu_foto.jpg")
        input("Presiona Enter...")
        sys.exit()
    
    try:
        img = face_recognition.load_image_file(sys.argv[1])
        caras = face_recognition.face_landmarks(img)
        if not caras:
            print("No se detectó cara. Usa foto frontal clara.")
            input("Enter...")
            sys.exit()
        
        forma, fade = analizar_forma_cara(caras[0])
        print(f"\nFORMA DE CARA: {forma}")
        print(f"CORTE RECOMENDADO: {fade}")
        print(f"Lleva esto a tu barbería!\n")
        
        # Mostrar imagen con puntos
        rgb = cv2.cvtColor(img, cv2.COLOR_RGB2BGR)
        for puntos in caras[0].values():
            for p in puntos:
                cv2.circle(rgb, p, 2, (0,255,0), -1)
        cv2.putText(rgb, f"{forma} -> {fade}", (10,30), cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0,255,0), 2)
        cv2.imshow("BarberIA - Tu Corte Ideal", rgb)
        cv2.waitKey(0)
        cv2.destroyAllWindows()
        
    except Exception as e:
        print(f"Error: {e}")
        input("Enter...")