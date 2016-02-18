w4-inflow-dataconnector-zipncities
==================================

This project is a dataconnector for W4 Inflow that act as a repository for all French zipcodes and cities.

Ce projet est un connecteur de données (dataconnector) pour W4 permettant de servir de référentiel pour l'ensemble des code postaux et noms des villes françaises.


Installation
------------

### Building from source

Using maven, run 

    mvn package
    
Then extract the generated package, either zip or tar.gz, at the root of a W4 BPMN+ Engine installation. It will create the necessary entries into `lib/dataConnectorDrivers/contrib` subdirectory of W4 BPMN+ Engine.


Usage
-----

In W4 Inflow, in the configuration, add a dataconnector with name `zipncities`


License
-------

Copyright (c) 2016, W4 SAS 

This project is licensed under the terms of the MIT License (see LICENSE file).

The [French zip database](https://www.data.gouv.fr/fr/datasets/base-officielle-des-codes-postaux/) (data.csv) is published by Groupe LaPoste under ODbL License.



Ce projet est licencié sous les termes de la licence MIT (voir le fichier LICENSE).

La [base officielle des code postaux](https://www.data.gouv.fr/fr/datasets/base-officielle-des-codes-postaux/) (data.csv) est publiée par le Groupe LaPoste sous la license ODbL
