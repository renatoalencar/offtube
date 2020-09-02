#!/bin/sh

rm target/*.jar

lein uberjar

JARNAME=$(ls ./target/*.jar | head -n1)

rsync $JARNAME root@161.35.107.35:/home/medda/

ssh root@161.35.107.35 'systemctl restart medda'
