#!/bin/bash
##################################################################################################################
#Gatling scale out/cluster run script:
#Before running this script some assumptions are made:
#1) Public keys were exchange inorder to ssh with no password promot (ssh-copy-id on all remotes)
#2) Check  read/write permissions on all folders declared in this script.
#3) Gatling installation (GATLING_HOME variable) is the same on all hosts
#4) Assuming all hosts has the same user name (if not change in script)
##################################################################################################################

#Assuming same user name for all hosts
USER_NAME='robert'

#Remote hosts list
HOSTS=( localhost )

#Assuming all Gatling installation in same path (with write permissions)
GATLING_HOME=/Users/robert/Desktop/gatling-charts-highcharts-bundle-3.5.1
GATLING_SIMULATIONS_DIR=$GATLING_HOME/user-files/simulations
GATLING_RUNNER=$GATLING_HOME/bin/gatling.sh

#Change to your simulation class name

SIMULATION_NAME='loadSimulation.loadSimulation'
#SIMULATION_NAME='saveSimulation.SaveSimulation'

#No need to change this
GATLING_REPORT_DIR=$GATLING_HOME/results/
GATHER_REPORTS_DIR=/gatling/reports/

echo "Starting Gatling cluster run for simulation: $SIMULATION_NAME"

#echo "Cleaning previous runs from localhost"
#rm -rf $GATHER_REPORTS_DIR
mkdir $GATHER_REPORTS_DIR
#rm -rf $GATLING_REPORT_DIR

echo "Running simulation on localhost"
$GATLING_RUNNER -nr -s $SIMULATION_NAME

echo "Gathering result file from localhost"
ls -t $GATLING_REPORT_DIR | head -n 1 | xargs -I {} mv ${GATLING_REPORT_DIR}{} ${GATLING_REPORT_DIR}report
cp ${GATLING_REPORT_DIR}report/simulation.log $GATHER_REPORTS_DIR


for HOST in "${HOSTS[@]}"
do
  echo "Gathering result file from host: $HOST"
  ssh -n -f $USER_NAME@$HOST "sh -c 'ls -t $GATLING_REPORT_DIR | head -n 1 | xargs -I {} mv ${GATLING_REPORT_DIR}{} ${GATLING_REPORT_DIR}report'"
  scp $USER_NAME@$HOST:${GATLING_REPORT_DIR}report/simulation.log ${GATHER_REPORTS_DIR}simulation-$HOST.log
done

mv $GATHER_REPORTS_DIR $GATLING_REPORT_DIR
echo "Aggregating simulations"
$GATLING_RUNNER -ro reports

#using macOSX
open ${GATLING_REPORT_DIR}reports/index.html

#using ubuntu
#google-chrome ${GATLING_REPORT_DIR}reports/index.html