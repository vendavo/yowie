# Yowie     
 
 It's a mesos framework for running docker containers (http://mesos.apache.org/documentation/latest/mesos-frameworks/). 
 There are frameworks like Marathon, Aurora for running applications and keep them up & running forever. 
 There are also frameworks like Chronos to schedule jobs and manage flow: run next when first finished.
   
 However there is no framework which can do advanced flow:
 
 ![General flow][generalFlow]
 
1. DB container is started first
2. Once is in state "Ready" then Application Container is started
3. Application container must go through "Starting" and "Web Server Starting" phases
4. Once it reaches "Ready" then Load Test Container is started
5. It loads test data, run load test and when it's done all containers finishes

This type of flow is needed for automatic integration / performance tests. 
It's not enough to start container when other finishes. It's needed to be able start next container(s) 
when some reached wanted status (e.g. Ready).

For more information about REST and types, see: https://github.com/vendavo/yowie/wiki/REST

## Licence

This project is under MIT license.

## Features

* Core features
    * Can run single tasks
    * Can run group of tasks
        * Including advanced flow with dependencies
        * Terminate task & status
          * It's possible to specify which task & status combination is final. So when the task reaches the status Yowie will kill all tasks in group.
        * Constraints
          * It's possible to restrict where the container must be run by setting attributes on mesos slave and specify them in request
          * Yowie always check available resources for memory, cpu, disk ports etc so it'll start a container on slave closest to the requirements (if constraints are matched)
    * Port mapping, volumes etc
        * Yowie supports all standard docker container settings

* REST API to control Yowie
* Web UI
    * There is a web ui written in AngularJS & sockets to have real time information about tasks and groups

## How to build

Needed:

* Java 8
* Gradle 2.4+

and just run 

```
gradle clean build
```


## How to run

Yowie is based on Spring Boot. So once it's built it can be run as standalone application (jar or via sh script in distribution archive).
However there is also Docker support:

```
docker run -d --net=host -p 8080:8080 -e "LIBPROCESS_IP=10.60.11.80" -e "mesos.url=10.60.11.80" -e "yowie.framework.externalUrl=http://10.60.11.80:8080" registry_url/yowie_0.21.1:0.1.0-SNAPSHOT
```

where:

* ``` --net=host ``` Yowie needs to share host's network as mesos needs to establish direct TCP connection w/ it's frameworks 
* ``` mesos.url ``` URL to mesos master
* ``` yowie.framework.externalUrl ``` External URL to reach yowie docker container. It's needed to have Yowie works properly
* ``` LIBPROCESS_IP ``` must be set to host IP where docker container is run
* ``` MESOS_NATIVE_JAVA_LIBRARY  ``` it's already set in docker container. However if you run Yowie as standalone you need to provide absolute path to mesos natives
* ``` LIBPROCESS_PORT ``` optional parameter to specify on which port will Yowie listen to mesos.



## Examples

### Simple task request

Run task w/ name __micro-benchmark-3227__, __4__ cores and __14700__ memory available. Docker container to be run is __registry_url/eps-benchmarks-runner:v2__ and bunch of environment variables.

```javascript 
{
  "name": "micro-benchmark-3227",
  "cpus": 4,
  "mem": 14700,
  "container": {
    "image": "registry_url/eps-benchmarks-runner:v2",
    "network": "BRIDGE"
  },
  "env": {
    "BUILD_NUMBER": "3227",
    "ES_URL": "http://es-benchmarks.dc.vendavo.com",
    "BUILD_TYPE": "jenkins"
  }
}
```

### Group w/ dependencies

 ![Group /w dependencies][complex1]


```javascript 
{
  "name": "group-1",
  "tasks": [
    {
      "name": "dep-tester-1",
      "cpus": 1,
      "mem": 64,
      "container": {
        "image": "registry_url/yowie-dep-tester:v1",
        "network": "BRIDGE"
      }
    },
    {
      "name": "dep-tester-2",
      "cpus": 1,
      "mem": 64,
      "container": {
        "image": "registry_url/yowie-dep-tester:v1",
        "network": "BRIDGE"
      },
      "dependsOn": {
        "name": "dep-tester-1",
        "status": "UP"
      }
    },
    {
      "name": "dep-tester-3",
      "cpus": 1,
      "mem": 64,
      "container": {
        "image": "registry_url/yowie-dep-tester:v1",
        "network": "BRIDGE"
      },
      "dependsOn": {
        "name": "dep-tester-2",
        "status": "STATE_1"
      }
    },
    {
      "name": "dep-tester-4",
      "cpus": 1,
      "mem": 64,
      "container": {
        "image": "registry_url/yowie-dep-tester:v1",
        "network": "BRIDGE"
      },
      "dependsOn": {
        "name": "dep-tester-3",
        "status": "STATE_2"
      }
    },
    {
      "name": "dep-tester-5",
      "cpus": 1,
      "mem": 64,
      "container": {
        "image": "registry_url/yowie-dep-tester:v1",
        "network": "BRIDGE"
      },
      "dependsOn": {
        "name": "dep-tester-1",
        "status": "UP"
      }
    },
    {
      "name": "dep-tester-6",
      "cpus": 1,
      "mem": 64,
      "container": {
        "image": "registry_url/yowie-dep-tester:v1",
        "network": "BRIDGE"
      },
      "dependsOn": {
        "name": "dep-tester-3",
        "status": "UP"
      }
    }
  ]
}
```

### Group w/ dependencies & constraints

Same example as above w/ added constraints

```javascript 
{
  "name": "group-1",
  "tasks": [
    {
      "name": "dep-tester-1",
      "cpus": 1,
      "mem": 64,
      "container": {
        "image": "registry_url/yowie-dep-tester:v2",
        "network": "BRIDGE",
        "privileged": true,
        "portMappings": [
          {"containerPort": 8080, "hostPort": 31101}
         ],
        "volumes": [
          {"containerPath": "/mnt/anything", "hostPath": "/mnt/something/else"}
         ]
      },
      "constraints": [
        {"name": "name", "value": "mesos-slave-1"}
      ]
    },
    {
      "name": "dep-tester-2",
      "cpus": 1,
      "mem": 64,
      "container": {
        "image": "registry_url/yowie-dep-tester:v2",
        "network": "BRIDGE",
        "portMappings": [
          {"containerPort": 8080, "hostPort": 31102}
         ]
      },
      "dependsOn": {
        "name": "dep-tester-1",
        "status": "UP"
      },
      "constraints": [
        {"name": "name", "value": "mesos-slave-2"}
      ]
    },
    {
      "name": "dep-tester-3",
      "cpus": 1,
      "mem": 64,
      "container": {
        "image": "registry_url/yowie-dep-tester:v2",
        "network": "BRIDGE",
        "portMappings": [
          {"containerPort": 8080, "hostPort": 31103}
         ]
      },
      "dependsOn": {
        "name": "dep-tester-2",
        "status": "STATE_1"
      },
      "constraints": [
        {"name": "name", "value": "mesos-slave-3"}
      ]
    },
    {
      "name": "dep-tester-4",
      "cpus": 1,
      "mem": 64,
      "container": {
        "image": "registry_url/yowie-dep-tester:v2",
        "network": "BRIDGE",
        "portMappings": [
          {"containerPort": 8080, "hostPort": 31104}
         ]
      },
      "dependsOn": {
        "name": "dep-tester-3",
        "status": "STATE_2"
      },
      "constraints": [
        {"name": "name", "value": "mesos-slave-4"}
      ]
    },
    {
      "name": "dep-tester-5",
      "cpus": 1,
      "mem": 64,
      "container": {
        "image": "registry_url/yowie-dep-tester:v2",
        "network": "BRIDGE",
        "portMappings": [
          {"containerPort": 8080, "hostPort": 31105}
         ]
      },
      "dependsOn": {
        "name": "dep-tester-1",
        "status": "UP"
      },
      "constraints": [
        {"name": "name", "value": "mesos-slave-1"}
      ]
    },
    {
      "name": "dep-tester-6",
      "cpus": 1,
      "mem": 64,
      "container": {
        "image": "registry_url/yowie-dep-tester:v2",
        "network": "BRIDGE",
        "portMappings": [
          {"containerPort": 8080, "hostPort": 31106}
         ]
      },
      "dependsOn": {
        "name": "dep-tester-3",
        "status": "UP"
      },
      "constraints": [
        {"name": "name", "value": "mesos-slave-2" }
      ]
    }
  ]
}
```

### Group w/ terminate task

```javascript
{
  "name": "group-1",
  "terminateTask": {
    "name": "dep-tester-2",
    "status": "STATE_1"
  }
  ...
}
```

[generalFlow]: markdown-resources/general_flow.png "General Flow"
[complex1]: markdown-resources/group_w_dependencies.png "General Flow"
