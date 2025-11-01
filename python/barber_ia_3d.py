import face_recognition
import cv2
import numpy as np
import sys
import os

def distancia(p1, p2):
    return np.sqrt((p1[0] - p2[0])**2 + (p1[1] - p2[1])**2)

def cargar_imagen(ruta):
    """Carga imagen con soporte para rutas con acentos"""
    if not os.path.exists(ruta):
        return None
    img_array = np.fromfile(ruta, dtype=np.uint8)
    img = cv2.imdecode(img_array, cv2.IMREAD_COLOR)
    if img is None:
        return None
    # Redimensionar si es muy grande
    h, w = img.shape[:2]
    if max(w, h) > 800:
        escala = 800 / max(w, h)
        img = cv2.resize(img, (int(w * escala), int(h * escala)), interpolation=cv2.INTER_AREA)
    return cv2.cvtColor(img, cv2.COLOR_BGR2RGB)

def detectar_landmarks(img_rgb):
    landmarks_list = face_recognition.face_landmarks(img_rgb)
    return landmarks_list[0] if landmarks_list else None

def analizar_frente(landmarks):
    chin = landmarks['chin']
    left_eyebrow = landmarks['left_eyebrow'][0]
    right_eyebrow = landmarks['right_eyebrow'][-1]
    
    face_width = distancia(chin[0], chin[16])
    forehead_y = min(left_eyebrow[1], right_eyebrow[1]) - int(face_width * 0.1)
    forehead_center = ((left_eyebrow[0] + right_eyebrow[0]) // 2, forehead_y)
    chin_center = chin[8]
    face_height = distancia(forehead_center, chin_center)
    ratio = face_height / face_width if face_width > 0 else 1.0
    return ratio, face_width

def analizar_perfil(landmarks):
    try:
        nose = landmarks['nose_bridge'][0]
        chin = landmarks['chin'][8]
        jaw_depth = distancia(nose, chin)
        return "FUERTE" if jaw_depth > 180 else "SUAVE"
    except:
        return "NORMAL"

def analizar_atras(img_rgb):
    gray = cv2.cvtColor(img_rgb, cv2.COLOR_RGB2GRAY)
    blurred = cv2.GaussianBlur(gray, (9, 9), 2)
    circles = cv2.HoughCircles(blurred, cv2.HOUGH_GRADIENT, 1, 200,
                               param1=50, param2=30, minRadius=80, maxRadius=300)
    if circles is not None:
        circles = np.round(circles[0, :]).astype("int")
        radius = circles[0][2]
        return "CABEZA REDONDA" if radius > 150 else "CABEZA ALARGADA"
    return "FORMA ESTÁNDAR"

if __name__ == "__main__":
    if len(sys.argv) < 4:
        print("Uso: py barber_ia_3d.py frente.jpg lado.jpg atras.jpg")
        input("\nPresiona Enter...")
        sys.exit()
    
    frente_path, lado_path, atras_path = sys.argv[1], sys.argv[2], sys.argv[3]
    
    print("Cargando imágenes...")
    img_frente = cargar_imagen(frente_path)
    img_lado = cargar_imagen(lado_path)
    img_atras = cargar_imagen(atras_path)
    
    # VERIFICAR QUE TODAS LAS IMÁGENES SE CARGARON
    if img_frente is None:
        print(f"No se pudo cargar: {frente_path}")
        input("Enter...")
        sys.exit()
    if img_lado is None:
        print(f"No se pudo cargar: {lado_path}")
        input("Enter...")
        sys.exit()
    if img_atras is None:
        print(f"No se pudo cargar: {atras_path}")
        input("Enter...")
        sys.exit()
    
    print("Detectando rostros...")
    landmarks_frente = detectar_landmarks(img_frente)
    landmarks_lado = detectar_landmarks(img_lado)
    
    if not landmarks_frente:
        print("No se detectó cara en la foto de FRENTE.")
        cv2.imshow("Frente - Sin detección", cv2.cvtColor(img_frente, cv2.COLOR_RGB2BGR))
        cv2.waitKey(0)
        input("Enter...")
        sys.exit()
    
    if not landmarks_lado:
        print("No se detectó cara en la foto de LADO.")
        cv2.imshow("Lado - Sin detección", cv2.cvtColor(img_lado, cv2.COLOR_RGB2BGR))
        cv2.waitKey(0)
        input("Enter...")
        sys.exit()
    
    # ANÁLISIS 3D
    ratio, face_width = analizar_frente(landmarks_frente)
    jaw_strength = analizar_perfil(landmarks_lado)
    head_shape = analizar_atras(img_atras)
    
    # DETERMINAR FORMA
    if ratio > 1.5:
        forma = "ALARGADA"
        fade = "LOW FADE + MULLET"
    elif ratio < 1.05:
        forma = "REDONDA"
        fade = "HIGH SKIN FADE"
    else:
        forma = "OVALADA"
        fade = "MID FADE + TAPER"
    
    # MOSTRAR RESULTADO
    print(f"\n{'='*60}")
    print(f"   BARBERÍA IA 3D - ANÁLISIS COMPLETO")
    print(f"{'='*60}")
    print(f"   Forma de cara: {forma}")
    print(f"   Ratio alto/ancho: {ratio:.2f}")
    print(f"   Mandíbula: {jaw_strength}")
    print(f"   Cabeza (atrás): {head_shape}")
    print(f"{'='*60}")
    print(f"   CORTE RECOMENDADO:")
    print(f"   → {fade}")
    if "MULLET" in fade:
        print(f"   → MULLET: SÍ (largo atrás)")
    if "TAPER" in fade:
        print(f"   → TAPER: SÍ (degradado suave)")
    print(f"{'='*60}\n")
    
    # MOSTRAR IMÁGENES CON PUNTOS
    def dibujar_landmarks(img_rgb, landmarks, color=(0,255,0)):
        img_bgr = cv2.cvtColor(img_rgb, cv2.COLOR_RGB2BGR)
        for puntos in landmarks.values():
            for p in puntos:
                cv2.circle(img_bgr, p, 2, color, -1)
        return img_bgr
    
    frente_bgr = dibujar_landmarks(img_frente, landmarks_frente)
    lado_bgr = dibujar_landmarks(img_lado, landmarks_lado, (255,0,0))
    atras_bgr = cv2.cvtColor(img_atras, cv2.COLOR_RGB2BGR)
    
    cv2.putText(frente_bgr, f"{forma}", (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0,255,0), 2)
    cv2.putText(lado_bgr, f"Mand: {jaw_strength}", (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (255,0,0), 2)
    cv2.putText(atras_bgr, head_shape, (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0,0,255), 2)
    
    cv2.imshow("FRENTE", frente_bgr)
    cv2.imshow("LADO", lado_bgr)
    cv2.imshow("ATRÁS", atras_bgr)
    cv2.waitKey(0)
    cv2.destroyAllWindows()
    
    input("Presiona Enter para salir...")