#include <Servo.h>
#include "ESP8266WiFi.h"
#include "ThingSpeak.h"
#include <SPI.h>
#include "secrets.h"
#include <ESP8266HTTPClient.h>

Servo servo1;

const int trigPin = D5;
const int echoPin = D6;

long duration;
int distance;
bool objectDetected = false;
unsigned long lastUpdateTime = 0;
const unsigned long updateInterval = 15000; // Espera 15 segundos para enviar a thingspeak
unsigned long lastObjectTime = 0;
const unsigned long delayBeforeLowering = 1000;
unsigned long lastReadTime = 0;

WiFiClient client;

void setup() {
  servo1.attach(D2);
  pinMode(trigPin, OUTPUT);
  pinMode(echoPin, INPUT);
  Serial.begin(9600);

  WiFi.begin(SECRET_SSID, SECRET_PASS);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nConectado a Wi-Fi");

  ThingSpeak.begin(client);
}

void loop() {
  unsigned long currentTime = millis();

  distance = getDistance();

  if (distance > 0 && distance < 10) {
    if (!objectDetected) {
      objectDetected = true;
      moveServoSmoothly(180);
      Serial.println("Objeto detectado, subiendo servo.");
    }
    lastObjectTime = millis();
  } else if (objectDetected && millis() - lastObjectTime >= delayBeforeLowering) {
    objectDetected = false;
    moveServoSmoothly(0);
    Serial.println("Objeto no detectado, bajando servo.");
  }

  if (currentTime - lastUpdateTime >= updateInterval) {
    lastUpdateTime = currentTime;
    sendToThingSpeak();
  }

  if (currentTime - lastReadTime >= updateInterval) {
    lastReadTime = currentTime;
    readAppControlFromThingSpeak();
  }

  delay(50);
}

int getDistance() {
  long totalDuration = 0;
  int numReadings = 5;

  for (int i = 0; i < numReadings; i++) {
    digitalWrite(trigPin, LOW);
    delayMicroseconds(2);
    digitalWrite(trigPin, HIGH);
    delayMicroseconds(10);
    digitalWrite(trigPin, LOW);

    totalDuration += pulseIn(echoPin, HIGH);
    delay(10);
  }

  long avgDuration = totalDuration / numReadings;
  int avgDistance = avgDuration * 0.034 / 2;
  return avgDistance;
}

void sendToThingSpeak() {
  ThingSpeak.setField(1, distance);
  ThingSpeak.setField(2, objectDetected ? 180 : 0);

  int httpCode = ThingSpeak.writeFields(SECRET_CH_ID, SECRET_WRITE_APIKEY);

  if (httpCode == 200) {
    Serial.println("Datos enviados a ThingSpeak con éxito");
  } else {
    Serial.print("Error al enviar datos a ThingSpeak: ");
    Serial.println(httpCode);
  }
}

void readAppControlFromThingSpeak() {
  int appServoPosition = ThingSpeak.readIntField(SECRET_CH_ID, 3, SECRET_READ_APIKEY);
  if (appServoPosition != -1) {
    if (appServoPosition == 0 || appServoPosition == 180) {
      // Mover el servo solo si es necesario
      if (servo1.read() != appServoPosition) {
        moveServoSmoothly(appServoPosition);
        Serial.print("Servo movido a: ");
        Serial.println(appServoPosition);
      }
    } else {
      Serial.println("Valor de ángulo no válido recibido desde la app, debe ser 0 o 180");
    }
  } else {
    Serial.println("Error al leer el control de la app desde ThingSpeak");
  }
}

void moveServoSmoothly(int targetAngle) {
  int currentAngle = servo1.read();
  int stepSize = 5;

  if (currentAngle < targetAngle) {
    for (int angle = currentAngle; angle <= targetAngle; angle += stepSize) {
      servo1.write(angle);
      delay(10);
    }
  } else {
    for (int angle = currentAngle; angle >= targetAngle; angle -= stepSize) {
      servo1.write(angle);
      delay(10);
    }
  }

  servo1.write(targetAngle);
}
