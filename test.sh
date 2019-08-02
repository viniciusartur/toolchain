#!/bin/bash
git clone -b feature/toolchain https://github.com/magwas/PDEngine.git
sed -i 'sA^build_image:.*Abuild_image: kodekonveyor/toolchain:masterA' PDEngine/shippable.yml 
cd PDEngine
pwd
DOCKER_IMAGE=kodekonveyor/toolchain:master ../inproject/tools/testenv Script

