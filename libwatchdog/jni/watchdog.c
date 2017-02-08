#include <stdlib.h>
#include <unistd.h>
#include <sys/prctl.h>

#include "watchdog.h"
#include "log.h"
#include "utils.h"

void parent_handler(int signum) {
    LOGE("watchdog signal : %d !!!", signum);
    if (signum == SIGHUP) {
        start_service_direct(PARENT_DEAD_ACTION);
    }
}

int main(int argc, char *argv[]) {
    setsid();
    int pipe_fd1[2];
    int pipe_fd2[2];
    int need;
    char *pkg_name;
    char *svc_name;
    int i;
    for (i = 0; i < argc; i++) {
        if (argv[i] == NULL) {
            continue;
        }
        if (!strcmp(PARAM_PKG_NAME, argv[i])) {
            pkg_name = argv[i + 1];
        } else if (!strcmp(PARAM_SVC_NAME, argv[i])) {
            svc_name = argv[i + 1];
        } else if (!strcmp(PARAM_PIPE_1_READ, argv[i])) {
            char *p1r = argv[i + 1];
            pipe_fd1[0] = atoi(p1r);
        } else if (!strcmp(PARAM_PIPE_1_WRITE, argv[i])) {
            char *p1w = argv[i + 1];
            pipe_fd1[1] = atoi(p1w);
        } else if (!strcmp(PARAM_PIPE_2_READ, argv[i])) {
            char *p2r = argv[i + 1];
            pipe_fd2[0] = atoi(p2r);
        } else if (!strcmp(PARAM_PIPE_2_WRITE, argv[i])) {
            char *p2w = argv[i + 1];
            pipe_fd2[1] = atoi(p2w);
        } else if (!strcmp(PARAM_PARAM_1, argv[i])) {
            char *n = argv[i + 1];
            need = atoi(n);
        }
    }
    char libFile[200];
    sprintf(libFile,"/data/data/%s/lib",pkg_name);
    char r_buf[100];
    int r_num;
    memset(r_buf, 0, sizeof(r_buf));
    if (need) {
        prctl(PR_SET_PDEATHSIG, SIGHUP);
        install_signal();
        signal(SIGHUP,parent_handler);
        close(pipe_fd1[0]);
        close(pipe_fd2[1]);
        r_num = read(pipe_fd2[0], r_buf, 100);
    } else {
        pid_t pid = fork();
        if (pid == 0) {
            install_signal();
            setsid();
            close(pipe_fd1[0]);
            close(pipe_fd2[1]);
            r_num = read(pipe_fd2[0], r_buf, 100);
        } else {
            _exit(EXIT_SUCCESS);
        }
    }
    LOGE("watchdog >>>>PARENT<<<< Dead !!");
    if(is_dir_exist(libFile)<0){
        LOGE("can't found %s ,we maybe uninstalled,start clean self.",libFile);
        kill_process_like(pkg_name);
        char cmdRm[200];
        sprintf(cmdRm,"/data/data/%s",pkg_name);//very danger ,we should find a way to double check
        int ret=  delete_dir_contents(cmdRm,1);
        LOGE("exit self after rm %d.",ret);
        exit(EXIT_SUCCESS);
    }else{
        LOGE("founded %s ,this is't uninstall restart self.",libFile);
    }
    int count = 0;
    while (count < 50) {
        start_service(pkg_name, svc_name);
        usleep(100000);
        count++;
    }
}

