#!/bin/bash
#
# run-cyc.sh
#
# Shell script to execute the cyc server
#
# After some time loading the world into memory you will see CYC(1):
# which is the SubL command prompt.
# [optional] 
# You can enter SubL expressions such as (+ 1 2) or (genls #$Person)
# or (all-genls #$Person) at the command line to verify Cyc's operation.
# 
# At this point the cyc http server is running and you can access
# Cyc directly via the local web browser.
# http://localhost:3602/cgi-bin/cyccgi/cg?cb-start
# You can browse cyc via the Guest account or perform updates by
# logging on as CycAdminstrator.

OPENCYC_RELEASE=opencyc-2.0


case `pwd` in
   */${OPENCYC_RELEASE}) 
   cd server/cyc/run
   ;;
   */${OPENCYC_RELEASE}/scripts) 
   cd ../server/cyc/run
   ;;
  *)
   cd ${OPENCYC_RELEASE}/server/cyc/run
esac

echo 'Launching CYC server at' `date` '...'
MIN_HEAP=1300m
MAX_HEAP=1300m
PERM_SIZE=98m

java  -Xms$MIN_HEAP -Xmx$MAX_HEAP -XX:MaxPermSize=$PERM_SIZE  -cp lib/cyc.jar:lib/subl.jar:lib/juni\
t.jar:resource:lib/ext:plugins com.cyc.tool.subl.jrtl.nativeCode.subLisp.SubLMain  -f "(progn (load\
 \"init/jrtl-release-init.lisp\")))" "$@"

echo 'Cyc server has shut down at' `date`
