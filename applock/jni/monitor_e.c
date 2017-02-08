/*************************************************************************
	> File Name: test.c
	> Author: 
	> Mail: 
	> Created Time: 一  3/ 9 19:48:17 2015
 ************************************************************************/

#include <jni.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <android/log.h>
#include <unistd.h>
#include <sys/inotify.h>
#include<pthread.h>
#include<errno.h>

/* 宏定义begin */
//清0宏
#define MEM_ZERO(pDest, destSize) memset(pDest, 0, destSize)

//LOG宏定义
#define LOG_TAG "jni_monitor"

#ifdef ALI_LOG
#define LOG_INFO(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOG_DEBUG(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOG_WARN(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define LOG_ERROR(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#else
#define LOG_INFO(...)
#define LOG_DEBUG(...)
#define LOG_WARN(...)
#define LOG_ERROR(...)
#endif

#define ACT_MONI_CLASS "com/drinker/alock/monitor/activitymonitor/DefaultActivityMonitor"

#define THREAD_ALIVE 0
#define THREAD_NEED_STOP 1
#define THREAD_DEAD 2

typedef struct param {
    JNIEnv *env;
    jobject object;
    int version;
} * param;

void check_event(void *arg);
char * get_watch_path(int version);
char * get_backup_watch_path(int version);
//int thread_is_exist(pthread_t thread_id);
void check(JNIEnv* env, jobject obj, int version);
void uncheck(JNIEnv* env, jobject obj);
void checking(int arg);
void check_end();
//全局变量
JavaVM *g_jvm = NULL;
jobject g_obj = NULL;
JNIEnv *g_env = NULL;

pthread_t tidp = NULL;

pthread_mutex_t g_mutex = PTHREAD_MUTEX_INITIALIZER;

volatile int g_need_stop = THREAD_DEAD;

/**
 * Native Methods
 */
static JNINativeMethod nativeMethods[] = {
		{ "check","(I)V",(void *) check },
		{ "uncheck","()V",(void*) uncheck}
};

void check_event(void *arg) {
     int result = check_begin();
     if (result != 0) {
        return;
     }
     checking((int)arg);
     check_end();
}

int check_begin() {
    if (g_jvm == NULL) {
        return -1;
    }
    //Attach主线程
    if((*g_jvm)->AttachCurrentThread(g_jvm, &g_env, NULL) != JNI_OK) {
       LOG_DEBUG("AttachCurrentThread() failed");
       return -1;
    }
    return 0;
}

void checking(int arg) {
    int version = arg;
    jclass cls;
    jmethodID mid;
    int watchDescriptor = -100000;
    struct inotify_event *p_buf = NULL;
    int fileDescriptor = inotify_init();
    if (fileDescriptor < 0) {
        LOG_DEBUG("inotify init failure");
        goto error;
    }
    char *path = get_watch_path(version);
    watchDescriptor = inotify_add_watch(fileDescriptor, path, IN_MODIFY);
    LOG_DEBUG(path);
    if (watchDescriptor < 0) {
       LOG_DEBUG("inotify_add_watch  backup");
       path = get_backup_watch_path(version);
       watchDescriptor = inotify_add_watch(fileDescriptor, path, IN_MODIFY);
       LOG_DEBUG(path);
       if (watchDescriptor < 0) {
           LOG_DEBUG("inotify_add_watch  failure");
           goto error;
       }
    }
        //分配缓存，以便读取event，缓存大小=一个struct inotify_event的大小，这样一次处理一个event
        p_buf = malloc(sizeof(struct inotify_event));
        if (p_buf == NULL) {
            LOG_DEBUG("malloc  failure");
            goto error;
        }
        cls = (*g_env)->GetObjectClass(g_env,g_obj);
        while (1) {
           pthread_mutex_lock(&g_mutex);
           if (g_need_stop == THREAD_NEED_STOP) {
               LOG_DEBUG("thread stop");
               g_need_stop = THREAD_DEAD;
               pthread_mutex_unlock(&g_mutex);
               goto error;
           }
           pthread_mutex_unlock(&g_mutex);
           //开始监听
           LOG_DEBUG("start observer");
           size_t readBytes = read(fileDescriptor, p_buf, sizeof(struct inotify_event));
           LOG_DEBUG("watch changed");
           if (cls == NULL) {
               LOG_DEBUG("cls is null");
           }
           jmethodID  callback = (*g_env)->GetMethodID(g_env, cls,"onChanged","()V");
           if (callback == NULL) {
              LOG_DEBUG("callback is null");
           }
           (*g_env)->CallVoidMethod(g_env, g_obj,callback, NULL);
       }
    error:
       if (p_buf != NULL) {
           free(p_buf);
       }
       if (fileDescriptor >=0 && fileDescriptor != -100000) {
           inotify_rm_watch(fileDescriptor, watchDescriptor);
       }
}

void check_end() {
    if (g_obj != NULL) {
          (*g_env)->DeleteGlobalRef(g_env, g_obj);
          g_obj = NULL;
    }
    //Detach主线程
    if (g_jvm != NULL) {
       if((*g_jvm)->DetachCurrentThread(g_jvm) != JNI_OK) {
          LOG_DEBUG("DetachCurrentThread() failed");
       }
    }
    pthread_exit(0);
}

char * get_watch_path(int version) {
    char * path;
    if (version >= 22) {
        path = "/dev/cpuctl/bg_non_interactive/tasks";
    } else if (version == 21) {
        path = "/dev/cpuctl/apps/bg_non_interactive/tasks";
    } else {
        path = "/dev/log/events";
    }
    return path;
}

char * get_backup_watch_path(int version) {
    char * path;
    if (version >= 22) {
        path = "/dev/cpuctl/tasks";
    } else if (version == 21) {
        path = "/dev/cpuctl/apps/tasks";
    } else {
        path = "/dev/log/events";
    }
    return path;
}

//int thread_is_exist(pthread_t thread_id) {
//    if (thread_id == NULL) {
//        return 0;
//    }
//    int kill_rc = pthread_kill(thread_id,0);
//    if(kill_rc == ESRCH || kill_rc == EINVAL) {
//        return 0;
//    }
//    else {
//        return 1;
//    }
//}

void check(JNIEnv* env, jobject object, int version) {
    LOG_DEBUG("check start");
    int need_return = 0;
    pthread_mutex_lock(&g_mutex);
    if (g_need_stop != THREAD_DEAD) {
        LOG_DEBUG("thread_is_exist");
        g_need_stop = THREAD_ALIVE;
        need_return = 1;
    }
    pthread_mutex_unlock(&g_mutex);
    if (need_return) {
        return;
    }
     //不能直接赋值(g_obj = obj)
    if (g_obj != NULL) {
        (*env)->DeleteGlobalRef(env, g_obj);
    }
    g_obj = (*env)->NewGlobalRef(env,object);
    if (pthread_create(&tidp, NULL, check_event, (void *)version) != 0) {
       (*env)->DeleteGlobalRef(env, g_obj);
       LOG_DEBUG("create thread failure");
    } else {
       pthread_mutex_lock(&g_mutex);
       g_need_stop = THREAD_ALIVE;
       pthread_mutex_unlock(&g_mutex);
       LOG_DEBUG("create thread success");
    }
}


void uncheck(JNIEnv* env, jobject obj) {
    if (g_need_stop == THREAD_ALIVE) {
        pthread_mutex_lock(&g_mutex);
        g_need_stop = THREAD_NEED_STOP;
        pthread_mutex_unlock(&g_mutex);
    } else {
        LOG_DEBUG("thread isn't exist");
    }
}

/**
 *
 */
static int registerNativeMethods(JNIEnv* env) {
	int result = JNI_ERR;
	/* look up the class */
	jclass clazz = (*env)->FindClass(env, ACT_MONI_CLASS);
	if (NULL != clazz) {
		if ((*env)->RegisterNatives(env, clazz, nativeMethods,
				sizeof(nativeMethods) / sizeof(nativeMethods[0])) == JNI_OK) {
			result = JNI_OK;
		}
	}
	return result;
}

/**
 *
 */
jint JNI_OnLoad(JavaVM* vm, void* reserved) {
	JNIEnv* env = NULL;
	jint result = JNI_ERR;
    g_jvm = vm;
	if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_4) == JNI_OK) {
		if (NULL != env && registerNativeMethods(env) == 0) {
			result = JNI_VERSION_1_4;
		}
	}
	return result;
}