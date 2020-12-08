# SAP S/4HANA Cloud Extensions - Display Created or Changed Sales Orders Using Business Event Handling
This repository contains the sample code for the [Display Created or Changed Sales Orders Using Business Event Handling tutorial](http://tiny.cc/s4-business-event-pull).

*This code is only one part of the tutorial, so please follow the tutorial before attempting to use this code.*

## Description

The [Display Created or Changed Sales Orders Using Business Event Handling tutorial](http://tiny.cc/s4-business-event-pull) showcases an application that reads the events related to sales order objects in the business event queue in SAP S/4HANA Cloud. The app lists the sales orders related to the events in the queue and shows the details of any sales order selected.

#### SAP Extensibility Explorer

This tutorial is one of multiple tutorials that make up the [SAP Extensibility Explorer](https://sap.com/extends4) for SAP S/4HANA Cloud.
SAP Extensibility Explorer is a central place where anyone involved in the extensibility process can gain insight into various types of extensibility options. At the heart of SAP Extensibility Explorer, there is a rich repository of sample scenarios which show, in a hands-on way, how to realize an extensibility requirement leveraging different extensibility patterns.


Requirements
-------------
- An account in SAP Cloud Platform with a subaccount in the Neo environment and an SAP Cloud Platform Java server of any size.
- An SAP S/4HANA Cloud tenant. **This is a commercial paid product.**
- [Java SE 8 Development Kit (JDK)](https://www.oracle.com/technetwork/java/javase/downloads/index.html) and [Apache Maven](http://maven.apache.org/download.cgi) to build the application.

Download and Installation
-------------
This repository is a part of the [Download the App](https://help.sap.com/viewer/1f6856bc0f3740ab877da49563de2e63/SHIP/en-US/f319d91e3686458f9357271dff1dd03c.html) step in the tutorial. Instructions for use can be found in that step.

[Please download the zip file by clicking here](https://github.com/SAP/s4hana-ext-business-event-pull/archive/master.zip) so that the code can be used in the tutorial.


Known issues
---------------------
If you are working with an SAP Cloud Platform _Trial_ account, you must add the following 2 properties to the destination so that the connection to SAP S/4HANA Cloud works:
```
proxyHost = proxy-trial.od.sap.biz
proxyPort = 8080
```

How to obtain support
---------------------
If you have issues with this sample, please open a report using [GitHub issues](https://github.com/SAP/s4hana-ext-business-event-pull/issues).

License
-------
Copyright © 2020 SAP SE or an SAP affiliate company. All rights reserved.
This file is licensed under the Apache Software License, version 2.0 except as noted otherwise in the [LICENSE file](LICENSES/Apache-2.0.txt).
