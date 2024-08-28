ARG BASE_IMAGE 

FROM ${BASE_IMAGE} as base 
ARG SELDON_CORE_VERSION 
WORKDIR /workspace
RUN python3 -m pip install seldon-core==${SELDON_CORE_VERSION} --no-cache-dir \ 
    && python3 -m pip install pipdeptree --no-cache-dir \ 
    && pipdeptree -f -l -p seldon-core  > constraints.txt

FROM scratch AS bin
COPY --from=base /workspace/constraints.txt /


