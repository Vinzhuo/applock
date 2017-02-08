#include <stdlib.h>
#include <stdio.h>
#include <sys/inotify.h>
#include <fcntl.h>
#include <sys/stat.h>

#include "log.h"

/**
 *  get the android version code
 */
int get_version() {
    char value[8] = "";
    __system_property_get("ro.build.version.sdk", value);
    return atoi(value);
}

/**
 *  stitch three string to one
 */
char *str_stitching(const char *str1, const char *str2, const char *str3) {
    char *result;
    result = (char *) malloc(strlen(str1) + strlen(str2) + strlen(str3) + 1);
    if (!result) {
        return NULL;
    }
    strcpy(result, str1);
    strcat(result, str2);
    strcat(result, str3);
    return result;
}

/**
 * get android context
 */
jobject get_context(JNIEnv *env, jobject jobj) {
    jclass thiz_cls = (*env)->GetObjectClass(env, jobj);
    jfieldID context_field = (*env)->GetFieldID(env, thiz_cls, "mContext", "Landroid/content/Context;");
    return (*env)->GetObjectField(env, jobj, context_field);
}


char *get_package_name(JNIEnv *env, jobject jobj) {
    jobject context_obj = get_context(env, jobj);
    jclass context_cls = (*env)->GetObjectClass(env, context_obj);
    jmethodID getpackagename_method = (*env)->GetMethodID(jobj, context_cls, "getPackageName", "()Ljava/lang/String;");
    jstring package_name = (jstring) (*env)->CallObjectMethod(env, context_obj, getpackagename_method);
    return (char *) (*env)->GetStringUTFChars(env, package_name, 0);
}


/**
 * call java callback
 */
void java_callback(JNIEnv *env, jobject jobj, char *method_name) {
    jclass cls = (*env)->GetObjectClass(env, jobj);
    jmethodID cb_method = (*env)->GetMethodID(env, cls, method_name, "()V");
    (*env)->CallVoidMethod(env, jobj, cb_method);
}

int mypopen(char *pstrCmd) {
    FILE *fp=NULL;
    fp = popen(pstrCmd, "r");
    if (fp) {
        pclose(fp);
        LOGE("Watch popen success %s", pstrCmd);
    }
    return 0;
}

void start_service_fork(char *action) {
    pid_t pid = fork();
    if (pid < 0) {
        //error, do nothing...
    } else if (pid == 0) {
        int count = 0;
        while (count < 6) {
            fork();
            count++;
            usleep(count * 1000);
        }
        LOGE("Watch fork start %s %d", action, count);
        execlp("am", "am", "startservice", "--user", "0", "-a", action, "--include-stopped-packages", (char *) NULL);
        exit(EXIT_SUCCESS);
    } else {
        waitpid(pid, NULL, 0);
    }
}

void start_service_direct(char *action) {
    int version = get_version();
    char strCmd[200] = {0};
    if (version >= 17 || version == 0) {
        sprintf(strCmd, "am startservice -a %s --user 0 --include-stopped-packages", action);
    } else {
        sprintf(strCmd, "am startservice -a %s --include-stopped-packages", action);
    }
    mypopen(strCmd);
}
/**
 * start a android service
 */
void start_service(char *package_name, char *service_name) {
    pid_t pid = fork();
    if (pid < 0) {
        //error, do nothing...
    } else if (pid == 0) {
        if (package_name == NULL || service_name == NULL) {
            exit(EXIT_SUCCESS);
        }
        LOGE("Watch startservice %s", service_name);
        int version = get_version();
        char *pkg_svc_name = str_stitching(package_name, "/", service_name);
        if (version >= 17 || version == 0) {
            execlp("am", "am", "startservice", "--user", "0", "-n", pkg_svc_name, "--include-stopped-packages", (char *) NULL);
        } else {
            execlp("am", "am", "startservice", "-n", pkg_svc_name, "--include-stopped-packages", (char *) NULL);
        }
        exit(EXIT_SUCCESS);
    } else {
        waitpid(pid, NULL, 0);
    }
}

void sig_handler(int signum) {
    LOGE("watchdog signal : %d !!!", signum);
}

void install_signal() {
    //防止子进程变成僵尸进程
    signal(SIGCHLD,SIG_IGN);
    signal(SIGINT, sig_handler);
    signal(SIGKILL, sig_handler);
    signal(SIGTERM, sig_handler);
}
