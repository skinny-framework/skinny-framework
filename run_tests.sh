#!/bin/sh

base_dir=`dirname $0`
cd ${base_dir}
./skinny db:migrate test
./skinny test

