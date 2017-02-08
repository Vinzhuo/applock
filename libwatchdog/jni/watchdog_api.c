#include <stdio.h>
#include <jni.h>
#include <dirent.h>
#include <unistd.h>
#include <assert.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <sys/inotify.h>
#include <stdlib.h>
#include <sys/wait.h>
#include <sys/prctl.h>
#include "log.h"
#include "watchdog.h"
#include "utils.h"

#define JAVA_CLASS_NAME "com/drinker/watchdog/strategy/WatchDogStrategy"

/**
 *  get the process pid by process name
 */
int find_pid_by_name(char *pid_name, int *pid_list){
    DIR *dir;
    struct dirent *next;
    int i = 0;
    pid_list[0] = 0;
    dir = opendir("/proc");
    if (!dir){
        return 0;
    }
    while ((next = readdir(dir)) != NULL){
        FILE *status;
        char proc_file_name[BUFFER_SIZE];
        char buffer[BUFFER_SIZE];
        char process_name[BUFFER_SIZE];

        if (strcmp(next->d_name, "..") == 0){
            continue;
        }
        if (!isdigit(*next->d_name)){
            continue;
        }
        sprintf(proc_file_name, "/proc/%s/cmdline", next->d_name);
        if (!(status = fopen(proc_file_name, "r"))){
            continue;
        }
        if (fgets(buffer, BUFFER_SIZE - 1, status) == NULL){
            fclose(status);
            continue;
        }
        fclose(status);
        sscanf(buffer, "%[^-]", process_name);
        if (strcmp(process_name, pid_name) == 0){
            pid_list[i ++] = atoi(next->d_name);
        }
    }
    if (pid_list){
        pid_list[i] = 0;
    }
    closedir(dir);
    return i;
}

/**
 *  kill all process by name
 */
void kill_zombie_process(char* zombie_name){
    int pid_list[200];
    int total_num = find_pid_by_name(zombie_name, pid_list);
    if (total_num > 0) {
        LOGD("zombie process name is %s, and number is %d, killing...", zombie_name, total_num);
    }
    int i;
    for (i = 0; i < total_num; i ++)    {
        int retval = 0;
        int daemon_pid = pid_list[i];
        if (daemon_pid > 1 && daemon_pid != getpid() && daemon_pid != getppid()){
            retval = kill(daemon_pid, SIGTERM);
            if (!retval){
                LOGD("kill zombie successfully, zombie`s pid = %d", daemon_pid);
            }else{
                LOGE("kill zombie failed, zombie`s pid = %d", daemon_pid);
            }
        }
    }
}

static jobject g_jobj = NULL;
static JavaVM* g_jvm = NULL;
void child_handler(int signum) {
    LOGE("watchdog signal : %d !!!", signum);
    if (signum == SIGCHLD) {
        signal(SIGCHLD,SIG_IGN);
        start_service_direct(PARENT_DEAD_ACTION);
        pid_t pid;
        int stat;
        if (g_jobj != NULL && g_jvm != NULL) {
            JNIEnv *env = NULL;
            if ((*g_jvm)->GetEnv(g_jvm, (void**) &env, JNI_VERSION_1_4) != JNI_OK) {
                //Attach主线程
                if((*g_jvm)->AttachCurrentThread(g_jvm, &env, NULL) == JNI_OK) {
                    java_callback(env, g_jobj, CHILD_DEAD_CALLBACK);
                    (*g_jvm)->DetachCurrentThread(g_jvm);
                }
            } else {
                java_callback(env, g_jobj, CHILD_DEAD_CALLBACK);
            }

        }
        pid = waitpid(-1, &stat, WNOHANG);
    }
}

void parent_handler(int signum) {
    LOGE("watchdog signal : %d !!!", signum);
    if (signum == SIGHUP) {
        char value[8] = "";
        __system_property_get("ro.product.model", value);
        int i = 0;
        while (value[i] != '\0') {
            value[i] = tolower(value[i]);
            i++;
        }
        LOGE("watchdog value : %s !!!", value);
        if (get_version() >= 20 && strcmp(value, "mx4") == 0) {
            start_service_fork(PARENT_DEAD_ACTION);
        } else {
            start_service_direct(PARENT_DEAD_ACTION);
        }
        exit(EXIT_SUCCESS);
    }
}

void monitor_child(jobject jobj) {
    g_jobj = jobj;
    signal(SIGCHLD,child_handler);
}
void JNICALL start_execlp(JNIEnv *env, jobject jobj, jstring packetName, jstring serviceName,
                   jstring executablePath, jboolean needParent) {
    if(packetName == NULL || serviceName == NULL || executablePath == NULL){
        LOGE("watchdog start_execlp params cannot be NULL!");
        return ;
    }

    char *pkg_name = (char*)(*env)->GetStringUTFChars(env, packetName, 0);
    char *svc_name = (char*)(*env)->GetStringUTFChars(env, serviceName, 0);
    char *daemon_path = (char*)(*env)->GetStringUTFChars(env, executablePath, 0);

    kill_zombie_process(NATIVE_DAEMON_NAME);

    int pipe_fd1[2];//order to watch child
    int pipe_fd2[2];//order to watch parent

    pid_t pid;
    char r_buf[100];
    int r_num;
    memset(r_buf, 0, sizeof(r_buf));
    if(pipe(pipe_fd1)<0){
        LOGE("pipe1 create error");
        return ;
    }
    if(pipe(pipe_fd2)<0){
        LOGE("pipe2 create error");
        return ;
    }

    char str_p1r[10];
    char str_p1w[10];
    char str_p2r[10];
    char str_p2w[10];

    sprintf(str_p1r,"%d",pipe_fd1[0]);
    sprintf(str_p1w,"%d",pipe_fd1[1]);
    sprintf(str_p2r,"%d",pipe_fd2[0]);
    sprintf(str_p2w,"%d",pipe_fd2[1]);

    char need[1];
    sprintf(need,"%d",needParent);

    install_signal();
    if((pid=fork())==0){
        execlp(daemon_path,
               NATIVE_DAEMON_NAME,
               PARAM_PKG_NAME, pkg_name,
               PARAM_SVC_NAME, svc_name,
               PARAM_PIPE_1_READ, str_p1r,
               PARAM_PIPE_1_WRITE, str_p1w,
               PARAM_PIPE_2_READ, str_p2r,
               PARAM_PIPE_2_WRITE, str_p2w,
               PARAM_PARAM_1, need,
               (char *) NULL);
    }else if(pid>0){
        if (needParent) {
            monitor_child(jobj);
        }
        close(pipe_fd1[1]);
        close(pipe_fd2[0]);
        char libFile[200];
        sprintf(libFile,"/data/data/%s/lib",pkg_name);
        //wait for child
        r_num=read(pipe_fd1[0], r_buf, 100);
        LOGE("watchdog >>>>CHILD<<<< Dead !!!");
        if(is_dir_exist(libFile)<0){
            LOGE("can't found %s ,we maybe uninstalled,start clean self..",libFile);
            pid_t pid2;
            if((pid2=fork())==0){
               usleep(100);
               int ret;
               char cmdRm[200];
               sprintf(cmdRm,"/data/data/%s",pkg_name);
               kill_process_like(pkg_name);
               ret= delete_dir_contents(cmdRm,1);
               LOGE("exit self after rm %d.",ret);
               exit(EXIT_SUCCESS);
             }else{
                 LOGE("exit self.");
                 exit(EXIT_SUCCESS);
             }

        }else{
            LOGE("founded %s ,this is't uninstall restart self.",libFile);
        }
        if (needParent) {
            start_service_direct(PARENT_DEAD_ACTION);
        }
        java_callback(env, jobj, DAEMON_CALLBACK_NAME);
        close(pipe_fd1[0]);
        close(pipe_fd2[1]);
    }

}

void notify_and_waitfor(char *a2_path, char *b2_path) {
    int observer_self_descriptor = open(a2_path, O_RDONLY);
    if (observer_self_descriptor == -1) {
        observer_self_descriptor = open(a2_path, O_CREAT, S_IRUSR | S_IWUSR);
    }
    int observer_daemon_descriptor = open(b2_path, O_RDONLY);
    while (observer_daemon_descriptor == -1) {
        usleep(1000);
        observer_daemon_descriptor = open(b2_path, O_RDONLY);
    }
    remove(b2_path);
    LOGE("Watched >>>>OBSERVER<<<< has been ready...");
}

/**
 *  Lock the file, this is block method.
 */
int lock_file(char* lock_file_path){
    LOGD("start try to lock file >> %s <<", lock_file_path);
    int lockFileDescriptor = open(lock_file_path, O_RDONLY);
    if (lockFileDescriptor == -1){
        lockFileDescriptor = open(lock_file_path, O_CREAT, S_IRUSR);
    }

    int lockRet = flock(lockFileDescriptor, LOCK_EX);
    if (lockRet == -1){
        LOGE("lock file failed >> %s <<", lock_file_path);
        return 0;
    }else{
        LOGD("lock file success  >> %s <<", lock_file_path);
        return lockFileDescriptor;
    }
}

void unlock_file(char* lock_file_path,int lockFileDescriptor){
	int lockRet = flock(lockFileDescriptor, LOCK_UN);
	if (lockRet == -1){
		LOGE("unlock file failed >> %s <<", lock_file_path);
	}else{
//		LOGD("unlock file success  >> %s << %d", lock_file_path,lockFileDescriptor);
	}
	close(lockFileDescriptor);
}

void JNICALL start_lock_file(JNIEnv *env, jobject jobj, jstring a1Path, jstring b1Path,
                             jstring a2Path, jstring b2Path, jboolean need) {

    if (a1Path == NULL || b1Path == NULL || a2Path == NULL || b2Path == NULL) {
        LOGE("start_lock_file(), params don't be null.");
        return;
    }

    char *a1_path = (char *) (*env)->GetStringUTFChars(env, a1Path, 0);
    char *b1_path = (char *) (*env)->GetStringUTFChars(env, b1Path, 0);
    char *a2_path = (char *) (*env)->GetStringUTFChars(env, a2Path, 0);
    char *b2_path = (char *) (*env)->GetStringUTFChars(env, b2Path, 0);

    int lock_a1_status = 0;
    int lock_b1_status = 0;
    int try_time = 0;
     LOGE("Watch, path = %s ", a1_path);
    while (try_time < 3 && !(lock_a1_status = lock_file(a1_path))) {
        try_time++;
        LOGD("worker lock a1 failed and try again as %d times", try_time);
        usleep(10000);
    }
    if (!lock_a1_status) {
        LOGE("worker lock a1 failed and exit");
        return;
    }
    install_signal();
    notify_and_waitfor(a2_path, b2_path);
    if(need) {
        int pipe_fd1[2];//order to watch child
         pid_t pid;
         char r_buf[100];
         int r_num;
         memset(r_buf, 0, sizeof(r_buf));
         if(pipe(pipe_fd1)<0){
            LOGE("pipe1 create error");
            return ;
         }
        monitor_child(jobj);
        pid = fork();
         if (pid == 0) {
             prctl(PR_SET_PDEATHSIG, SIGHUP);
             install_signal();
             signal(SIGHUP,parent_handler);
             close(pipe_fd1[1]);
             r_num = read(pipe_fd1[0], r_buf, 100);
             LOGE("Watch >>>>parent<<<<< Daed !! ");
             start_service_direct(PARENT_DEAD_ACTION);
             exit(EXIT_SUCCESS);
         } else if(pid > 0) {
             close(pipe_fd1[0]);
             lock_b1_status = lock_file(b1_path);
             LOGE("Watch lock2 !! pid = %d",getpid());
         }
    } else{
        lock_b1_status = lock_file(b1_path);
        LOGE("Watch lock1 !! pid = %d",getpid());
    }

    if (lock_b1_status) {
        LOGE("Watch >>>>DAEMON<<<<< Daed !! pid = %d",getpid());
        unlock_file(a1_path, lock_a1_status);
        unlock_file(b1_path, lock_b1_status);
        remove(a2_path);// it`s important ! to prevent from deadlock
        remove(b2_path);// it`s important ! to prevent from deadlock
        java_callback(env, jobj, DAEMON_CALLBACK_NAME);
    }
}





static JNINativeMethod gMethods[] = {
        {"start", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Z)V", (void*)start_execlp},
        {"start", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Z)V", (void*)start_lock_file}
};



static int registerNativeMethods(JNIEnv* env, const char* className,
                                 JNINativeMethod* gMethods, int numMethods) {
    jclass clazz;
    clazz = (*env)->FindClass(env, className);
    if (clazz == NULL) {
        return JNI_FALSE;
    }
    if ((*env)->RegisterNatives(env, clazz, gMethods, numMethods) < 0) {
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

static int registerNatives(JNIEnv* env) {
    if (!registerNativeMethods(env, JAVA_CLASS_NAME, gMethods, sizeof(gMethods) / sizeof(gMethods[0])))
        return JNI_FALSE;

    return JNI_TRUE;
}


JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    jint result = -1;
    JNIEnv *env = NULL;
    if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        return -1;
    }
    assert(env != NULL);
    g_jvm = vm;
    if (!registerNatives(env)) {//注册
        return -1;
    }
    /* success -- return valid version number */
    result = JNI_VERSION_1_4;

    return result;
}