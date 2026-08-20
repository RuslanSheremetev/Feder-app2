#include <jni.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <time.h>

JNIEXPORT jstring JNICALL
Java_com_feder_compose_PhotoUploader_nativeUploadPhoto(JNIEnv *env, jobject thiz, jbyteArray photoBytes, jstring token) {
    // Получаем байты
    jsize len = (*env)->GetArrayLength(env, photoBytes);
    jbyte *bytes = (*env)->GetByteArrayElements(env, photoBytes, NULL);
    
    // Получаем токен
    const char *tokenStr = (*env)->GetStringUTFChars(env, token, NULL);
    
    // Создаём сокет
    int sock = socket(AF_INET, SOCK_STREAM, 0);
    if (sock < 0) return (*env)->NewStringUTF(env, NULL);
    
    struct sockaddr_in addr;
    addr.sin_family = AF_INET;
    addr.sin_port = htons(8002);
    inet_pton(AF_INET, "2.26.71.102", &addr.sin_addr);
    
    if (connect(sock, (struct sockaddr*)&addr, sizeof(addr)) < 0) {
        close(sock);
        return (*env)->NewStringUTF(env, NULL);
    }
    
    // Формируем multipart
    char boundary[64];
    snprintf(boundary, sizeof(boundary), "----JNI%d", (int)time(NULL));
    
    char header[4096];
    int headerLen = snprintf(header, sizeof(header),
        "POST /api/upload HTTP/1.1\r\n"
        "Host: 2.26.71.102:8002\r\n"
        "Authorization: Bearer %s\r\n"
        "Content-Type: multipart/form-data; boundary=%s\r\n"
        "Content-Length: %ld\r\n"
        "Connection: close\r\n"
        "\r\n",
        tokenStr, boundary,
        (long)(strlen(boundary) * 2 + strlen(tokenStr) + len + 200));
    
    // Отправляем header
    send(sock, header, headerLen, 0);
    
    // Отправляем multipart body
    char part1[256];
    int p1 = snprintf(part1, sizeof(part1), "--%s\r\nContent-Disposition: form-data; name=\"file\"; filename=\"photo.jpg\"\r\nContent-Type: image/jpeg\r\n\r\n", boundary);
    send(sock, part1, p1, 0);
    send(sock, bytes, len, 0);
    
    char part2[256];
    int p2 = snprintf(part2, sizeof(part2), "\r\n--%s--\r\n", boundary);
    send(sock, part2, p2, 0);
    
    // Читаем ответ
    char response[4096];
    int n = recv(sock, response, sizeof(response) - 1, 0);
    response[n] = '\0';
    
    close(sock);
    
    // Ищем URL
    char *url = strstr(response, "{\"url\":\"");
    if (url) {
        url += 8;
        char *end = strchr(url, '"');
        if (end) *end = '\0';
        jstring result = (*env)->NewStringUTF(env, url);
        (*env)->ReleaseByteArrayElements(env, photoBytes, bytes, 0);
        (*env)->ReleaseStringUTFChars(env, token, tokenStr);
        return result;
    }
    
    (*env)->ReleaseByteArrayElements(env, photoBytes, bytes, 0);
    (*env)->ReleaseStringUTFChars(env, token, tokenStr);
    return (*env)->NewStringUTF(env, NULL);
}
