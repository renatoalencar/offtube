#!/bin/sh

rm target/*.jar

lein uberjar

JARNAME=$(ls ./target/*.jar | head -n1)

# Eu achei medda como uma escrita errada de media, e achei que poderia
# ser um nome interessante.

rsync $JARNAME root@161.35.107.35:/home/medda/

ssh root@161.35.107.35 'systemctl restart medda'
