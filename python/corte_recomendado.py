import face_recognition
import cv2
import numpy as np
import sys
import os

def distancia(p1, p2):
    return np.sqrt((p1[0] - p2[0])**2 + (p1[1] - p2[1])**2)

def redimensionar_imagen(img, max_size=800):
    h, w = img.shape[:2]
    if max(w, h) > max_size:
        escala = max_size / max(w, h)
        img = cv2.resize(img, (int(w * escala), int(h * escala)), interpolation=cv2.INTER_AREA)
    return img

def analizar_forma_cara(landmarks):
    chin = landmarks['chin']
    left_eyebrow = landmarks['left_eyebrow']
    right_eyebrow = landmarks['right_eyebrow']
    
    forehead_center = (
        (left_eyebrow[0][0] + right_eyebrow[2][0]) // 2,
        min(left_eyebrow[0][1], right_eyebrow[2][1]) - int(distancia(left_eyebrow[0], right_eyebrow[2]) * 0.12)
    )
    chin_center = chin[8]
    face_width = distancia(chin[0], chin[16])
    face_height = distancia(forehead_center, chin_center)
    forehead_width = distancia(left_eyebrow[0], right_eyebrow[2])
    jaw_width = distancia(chin[4], chin[12])
    
    if face_width <= 0: face_width = 1
    ratio = face_height / face_width
    diff = (forehead_width - jaw_width) / face_width
    
    return ratio, diff, face_width, face_height

def forma_cara_str(ratio, diff):
    if ratio > 1.5:
        return "ALARGADA"
    elif ratio < 1.05:
        if diff < -0.1:
            return "CUADRADA"
        else:
            return "REDONDA"
    elif diff > 0.15:
        return "CORAZÓN"
    elif diff < -0.15:
        return "TRIÁNGULO"
    else:
        return "OVALADA"

def recomendar_corte(ratio, diff, face_width, face_height):
    forehead_width = face_width * (1 + diff) if diff > 0 else face_width * (1 - abs(diff))
    jaw_width = face_width * (1 - diff) if diff > 0 else face_width * (1 + abs(diff))

    # FADE
    if ratio > 1.5:
        fade = "LOW FADE"
        estilo = "Clásico con volumen arriba"
    elif ratio < 1.05:
        if jaw_width > forehead_width * 0.95:
            fade = "HIGH FADE"
            estilo = "Skin fade para resaltar mandíbula"
        else:
            fade = "HIGH FADE"
            estilo = "Burst fade para alargar"
    elif abs(diff) > 0.15:
        if diff > 0:
            fade = "LOW FADE"
            estilo = "Taper suave para equilibrar"
        else:
            fade = "LOW FADE"
            estilo = "Drop fade para mandíbula fuerte"
    else:
        fade = "MID FADE"
        estilo = "Balance perfecto"

    # MULLET
    mullet_ok = ratio > 1.3 and abs(diff) < 0.1
    mullet = "MULLET MODERNO (corto adelante, largo atrás)" if mullet_ok else "No recomendado"

    # TAPER
    taper = "TAPER FADE" if ratio < 1.4 else "TAPER CLASSIC"

    return fade, estilo, mullet, taper

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Uso: py corte_recomendado.py tu_foto.jpg")
        input("\nPresiona Enter...")
        sys.exit()
    
    ruta = sys.argv[1]
    if not os.path.exists(ruta):
        print(f"Archivo no encontrado: {ruta}")
        input("Enter...")
        sys.exit()
    
    try:
        img_cv = cv2.imdecode(np.fromfile(ruta, dtype=np.uint8), cv2.IMREAD_COLOR)
        if img_cv is None:
            print("Formato no soportado. Usa JPG, PNG, etc.")
            input("Enter...")
            sys.exit()
        
        img_cv = redimensionar_imagen(img_cv)
        img_rgb = cv2.cvtColor(img_cv, cv2.COLOR_BGR2RGB)
        
        caras = face_recognition.face_landmarks(img_rgb)
        if not caras:
            print("No se detectó cara. Usa foto frontal clara.")
            cv2.imshow("Sin detección", img_cv)
            cv2.waitKey(0)
            input("Enter...")
            sys.exit()
        
        landmarks = caras[0]
        ratio, diff, face_width, face_height = analizar_forma_cara(landmarks)
        fade, estilo, mullet, taper = recomendar_corte(ratio, diff, face_width, face_height)
        forma = forma_cara_str(ratio, diff)
        
        print(f"\n{'='*50}")
        print(f"   ANÁLISIS FACIAL COMPLETO")
        print(f"{'='*50}")
        print(f"   Forma de cara: {forma}")
        print(f"   Ratio alto/ancho: {ratio:.2f}")
        print(f"{'='*50}")
        print(f"   CORTE RECOMENDADO")
        print(f"   → FADE: {fade}")
        print(f"   → ESTILO: {estilo}")
        print(f"   → TAPER FADE: {taper}")
        print(f"   → MULLET: {mullet}")
        print(f"{'='*50}\n")
        
        # Dibujar puntos
        for puntos in landmarks.values():
            for p in puntos:
                cv2.circle(img_cv, p, 3, (0, 255, 0), -1)
        
        # Texto en imagen
        y = 50
        textos = [
            f"FORMA: {forma}",
            f"FADE: {fade}",
            f"TAPER: {taper}",
            f"MULLET: {'SÍ' if 'MODERNO' in mullet else 'NO'}"
        ]
        for txt in textos:
            cv2.putText(img_cv, txt, (10, y), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (255, 255, 0), 2)
            y += 35
        
        cv2.namedWindow("BarberIA PRO - Análisis Completo", cv2.WINDOW_NORMAL)
        cv2.imshow("BarberIA PRO - Análisis Completo", img_cv)
        cv2.waitKey(0)
        cv2.destroyAllWindows()
        
    except Exception as e:
        print(f"Error: {e}")
        input("Presiona Enter...")