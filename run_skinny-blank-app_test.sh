#!/bin/bash -x

rm -rf $HOME/.ivy2/cache/org.skinny-framework
rm -rf $HOME/.ivy2/local/org.skinny-framework
cd `dirname $0`
./publish_local.sh
./create_blank_app.sh test
cd release/skinny-blank-app

./skinny g controller help && \
./skinny g controller admin.help && \
./skinny g controller admin.sandbox api && \
./skinny g scaffold members1 member1 name:String activated:Boolean luckyNumber:Option[Long] birthday:Option[LocalDate] && \
./skinny g scaffold:scaml members2 member2 name:String activated:Boolean luckyNumber:Option[Long] birthday:Option[LocalDate] && \
./skinny g scaffold:jade members3 member3 name:String activated:Boolean luckyNumber:Option[Long] birthday:Option[LocalDate] && \
./skinny g scaffold admin members4 member4 name:String activated:Boolean luckyNumber:Option[Long] birthday:Option[LocalDate] && \
./skinny g scaffold:scaml admin members5 member5 name:String activated:Boolean luckyNumber:Option[Long] birthday:Option[LocalDate] && \
./skinny g scaffold:jade admin members6 member6 name:String activated:Boolean luckyNumber:Option[Long] birthday:Option[LocalDate] && \
./skinny db:migrate && \
./skinny db:migrate test && \
./skinny g reverse-model members1 rev1.member1 && \
./skinny g reverse-scaffold members2 rev1 members2 member2 && \
./skinny g reverse-model members1 rev2.member1 test && \
./skinny g reverse-scaffold members2 rev2.members2 member2 && \
./skinny routes && \
./skinny test && \
./skinny test:coverage && \
./skinny scalajs:package && \
./skinny package && \
./skinny package:standalone
