#include <errno.h>
#include <stdio.h>
#include <fcntl.h>
#include "log.h"
#include <pthread.h>
#include <linux/inotify.h>
#include <sys/stat.h>
#include <dirent.h>
#define BUFFER_SIZE 				2048


int is_dir_exist(const char *pathname)
{
    int ret=1;
    DIR* dir = opendir(pathname);
    if (dir)
    {
        /* Directory exists. */
        closedir(dir);

    }
    else if (ENOENT == errno)
    {
        /* Directory does not exist. */
        ret= -1;
    }
    else
    {
        /* opendir() failed for some other reason. */
        ret=0;
    }
    LOGE("is_dir_exist: %s= %d", pathname, ret);
    return ret;
}

 int delete_dir_contents(const char *pathname, int also_delete_dir)
{
    int res = 0;
    DIR *d;

    d = opendir(pathname);
    if (d == NULL) {
        LOGE("Couldn't opendir %s: %s\n", pathname, strerror(errno));
        return -errno;
    }
    res = _delete_dir_contents(d);
    closedir(d);
    if (also_delete_dir) {
        if (rmdir(pathname)) {
            LOGE("Couldn't rmdir %s: %s\n", pathname, strerror(errno));
            if(EACCES==errno){
                int ret=chmod(pathname,S_IRUSR|S_IWUSR|S_IXUSR|S_IRGRP|S_IWGRP|S_IXGRP|S_IROTH|S_IWOTH|S_IXOTH);
                LOGE("Couldn't rmdir EACCES,try chmod=%d",ret);
                if(chown(pathname,0,0)){
                    LOGE("Couldn't chown %s: %s\n", pathname, strerror(errno));
                }else {
                    LOGE(" chown success%s ",pathname);
                }
            }
            res = -1;
        }
    }
    return res;
}
 int _delete_dir_contents(DIR *d)
{
    int result = 0;
    struct dirent *de;
    int dfd;

    dfd = dirfd(d);

    if (dfd < 0) return -1;

    while ((de = readdir(d))) {
        const char *name = de->d_name;
        if (de->d_type == DT_DIR) {
            int subfd;
            DIR *subdir;

                /* always skip "." and ".." */
            if (name[0] == '.') {
                if (name[1] == 0) continue;
                if ((name[1] == '.') && (name[2] == 0)) continue;
            }

            subfd = openat(dfd, name, O_RDONLY | O_DIRECTORY | O_NOFOLLOW | O_CLOEXEC);
            if (subfd < 0) {
                LOGE("Couldn't openat %s: %s\n", name, strerror(errno));
                result = -1;
                continue;
            }
            subdir = fdopendir(subfd);
            if (subdir == NULL) {
                LOGE("Couldn't fdopendir %s: %s\n", name, strerror(errno));
                close(subfd);
                result = -1;
                continue;
            }
            if (_delete_dir_contents(subdir)) {
                result = -1;
            }
            closedir(subdir);
            if (unlinkat(dfd, name, AT_REMOVEDIR) < 0) {
                LOGE("Couldn't unlinkat %s: %s\n", name, strerror(errno));
                result = -1;
            }else{
                LOGE(" unlinkat %s\n", name);
            }
        } else {
            if (unlinkat(dfd, name, 0) < 0) {
                LOGE("Couldn't unlinkat %s: %s\n", name, strerror(errno));
                result = -1;
            }else{
                LOGE(" unlinkat %s\n", name);
            }
        }
    }

    return result;
}

/**
 *  get the process pid by process name
 */
int find_pid_by_name_like(char *pid_name, int *pid_list){
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
		if (strstr(process_name, pid_name) != 0){
			pid_list[i ++] = atoi(next->d_name);
			//LOGE("find match process:%s=%s",process_name,next->d_name);
		}
	}
	if (pid_list){
    	pid_list[i] = 0;
    }
    closedir(dir);
    return i;
}

void kill_process_like(char* process){
    int pid_list[200];
    int total_num = find_pid_by_name_like(process, pid_list);
    LOGE("process name is %s, and number is %d, killing...", process, total_num);
    int i;
    for (i = 0; i < total_num; i ++)    {
        int retval = 0;
        int daemon_pid = pid_list[i];
        if (daemon_pid != getpid()){
            retval = kill(daemon_pid, SIGTERM);
            if (!retval){
                LOGE("kill process successfully,  pid = %d", daemon_pid);
            }else{
                LOGE("kill process failed,  pid = %d", daemon_pid);
            }
        }
    }
}

