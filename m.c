#include<stdio.h>
#include<stdlib.h>
#include<stdbool.h>
#include<string.h>

typedef struct dir{
    char name[20];
    int isfile;
    char *content;
    struct dir *dirlist;
    struct dir *nextdir;
    struct dir *parent;
}Dir;


Dir* initialize(char *name, int isfile){
    Dir *newdir= (Dir*) calloc(1, sizeof(Dir));
    strcpy(newdir->name, name);
    newdir->isfile=  isfile;
    return newdir;
}

void add(Dir *dir, Dir *newdir){
    newdir->parent= dir;
    newdir->nextdir= dir->dirlist;
    dir->dirlist= newdir;
}

void create(Dir *dir, char *name, int isfile){
    Dir *newdir= initialize(name, isfile);
    add(dir, newdir);
}

void removal(Dir *dir, bool todelete){
    Dir *parentdir= dir->parent;
    Dir *prevdir;
    if(parentdir->dirlist!= dir){
        for(prevdir= parentdir->dirlist; prevdir->nextdir!= dir; prevdir= prevdir->nextdir);
        prevdir->nextdir= dir->nextdir;
    }
    else
        parentdir->dirlist= dir->nextdir;
    if(todelete)
        free(dir);
}

bool confiramtionforsearch(Dir *dir){
    printf("Found at: ");
    for(Dir *curr= dir; curr!= NULL; curr= curr->parent)
            printf("/%s", curr->name);
    printf("\n0.Confirm\t1.Ignore and conitnue searching: ");
    int ch;
    scanf("%d", &ch);
    return !ch;
}

Dir* search(Dir *dir, char *name, int isfile){
    Dir* subdir= dir->dirlist, *dirfromsub;
    while(subdir){
        if(!strcmp(subdir->name, name) && (subdir->isfile== isfile))
            if(confiramtionforsearch(subdir))
                return subdir;
        subdir= subdir->nextdir;
    }
    subdir= dir->dirlist;
    while(subdir){
        if(!subdir->isfile){
            dirfromsub= search(subdir, name, isfile);
            if(dirfromsub)
                if(confiramtionforsearch(dirfromsub))
                    return dirfromsub;
        }
        subdir= subdir->nextdir;
    }
    return NULL;
}

void delete(Dir *dir, char *name, bool isfile){
    Dir *subdir= search(dir, name, isfile);
    if(subdir)
        removal(subdir, true);
    else
        printf("Not Found\n");
}

int getsize(Dir *dir){
    int size= 0;
    for(Dir *subdir= dir->dirlist; subdir!= NULL; subdir= subdir->nextdir){
        size+= sizeof(subdir);
        if(subdir->isfile)
            size+= sizeof(subdir->content);
        else 
            size+= getsize(subdir);
    }
    return size;
}

void move(Dir *dir, Dir *newdir){
    if(dir== newdir){
        printf("What the heck r u doing man\n");
        return;
    }
    removal(dir, false);
    add(newdir, dir);
}

void display(Dir* dir){
    printf("%s:\n", dir->name);
    Dir *subdir;
    printf("Directories:\n");
    for(subdir= dir->dirlist; subdir!= NULL; subdir= subdir->nextdir){
        if(!subdir->isfile)
            printf("\t%s\n", subdir->name);
    }
    printf("Files:\n");
    for(subdir= dir->dirlist; subdir!= NULL; subdir= subdir->nextdir){
        if(subdir->isfile)
            printf("\t%s\n", subdir->name);
    }
}

Dir* goin(Dir* dir, char *name){
    Dir *subdir= search(dir, name, false);
    if(subdir)
        dir= subdir;
    else
        printf("Directory Not Found\n");
    return dir;
}

Dir* goout(Dir *dir){
    if(dir->parent== NULL)
        printf("You are inside outermost directory\n");
    else
        dir= dir->parent;
    return dir;
}

int main() {
    Dir *main= initialize("C", false);
    Dir *curr= main, *dir, *curr2, *dir2;
    int ch, ch2;
    char str[20], str2[1000];
    printf("1.Diplay currrent directory    2.Go inside a directory    3.Go outside the directory    4.Create    5.Delete    6.Move to a different directory    7.Open File    8.Display size    9.Exit\n");
    while (1) {
        printf("Current Directory: ");
        for(dir= curr; dir!= NULL; dir= dir->parent)
            printf("/%s", dir->name);
        printf("\nEnter your choice: ");
        scanf("%d", &ch);
        switch (ch) {
        case 1:
            display(curr);
            break;
        case 2:
            printf("Enter name of the directory: ");
            scanf("%s", &str);
            curr= goin(curr, str);
            break;
        case 3:
            curr= goout(curr);
            break;
        case 4:
            printf("0.For Directory    1.For File: ");
            fflush(stdin);
            scanf("%d", &ch2);
            printf("Enter name: ");
            fflush(stdin);
            scanf("%s", &str);
            create(curr, str, ch2);
            if(ch2== 1){
                printf("Enter content: ");
                fflush(stdin);
                gets(str2);
                curr->dirlist->content= (char*) calloc(strlen(str2), sizeof(char));
                strcpy(curr->dirlist->content, str2);
            }
            break;
        case 5:
            printf("0.For Directory    1.For File: ");
            scanf("%d", &ch2);
            printf("Enter name: ");
            scanf("%s", &str);
            delete(curr, str, ch2);
            break;
        case 6:
            printf("0.For Directory    1.For File: ");
            scanf("%d", &ch2);
            printf("Enter name: ");
            scanf("%s", &str);
            dir= search(curr, str, ch2);
            if(!dir){
                printf("Not Found\n");
                break;
            }
            ch2= 0;
            curr2= curr;
            while(ch2!= 3 && ch2!= 4){
                printf("Current Directory: ");
                for(dir2= curr2; dir2!= NULL; dir2= dir2->parent)
                    printf("/%s", dir2->name);
                printf("Go to the directory where the file is to be moved\n1.Move inside a directory    2.Move to the parent directory    3.Confirm directory    4.Cancel the operation\n");
                printf("\nEnter your choice: ");
                scanf("%d", &ch2);
                switch(ch2){
                case 1: 
                    printf("Enter name of the directory: ");
                    scanf("%s", &str);
                    curr2= goin(curr2, str);
                    break;
                case 2:
                    curr2= goout(curr2);
                    break;
                case 3:
                    removal(dir, false);        
                    add(curr2, dir);
                    curr= curr2;
                    break;
                case 4:
                    break;
                default:
                    printf("Enter proper choice.\n");
                }
            }
            break;
        case 7:
            printf("Enter name: ");
            scanf("%s", &str);
            dir= search(curr, str, 1);
            if(!dir){
                printf("Not Found\n");
                break;
            }
            while(ch2!= 4){
                printf("Current File: ");
                for(dir2= dir; dir2!= NULL; dir2= dir2->parent)
                printf("/%s", dir2->name);
                printf("\n1.Read    2.Write    3.Overwrite    4.Close\n");
                printf("\nEnter your choice: ");
                scanf("%d", &ch2);
                switch(ch2){
                case 1:
                    printf("%s\n", dir->content);
                    break;
                case 2:
                    printf("Enter content: ");
                    fflush(stdin);
                    gets(str2);
                    dir->content= (char*) realloc(dir->content, (strlen(str2)+strlen(dir->content))*sizeof(char));
                    strcat(dir->content, str2);
                    break;
                case 3:
                    printf("Enter content: ");
                    fflush(stdin);
                    gets(str2);
                    free(dir->content);
                    dir->content= (char*) calloc(strlen(str2), sizeof(char));
                    strcpy(dir->content, str2);
                    break;
                case 4:
                    break;
                default:
                    printf("Enter proper choice.\n");
                }
            }
            break;
        case 8:
            printf("Size: %d\n", sizeof(curr)+ getsize(curr));
            break;
        case 9:
            exit(0);
            break;
        default:
            fflush(stdin);
            printf("Enter proper choice.\n");
        }
    }

    return 0;
}
