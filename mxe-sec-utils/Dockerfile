FROM python:3.8-slim
WORKDIR /code

ENV VIRTUAL_ENV=/opt/venv
RUN python3 -m venv $VIRTUAL_ENV
ENV PATH="$VIRTUAL_ENV/bin:$PATH"

# Install dependencies:
COPY requirements.txt .
RUN pip install --upgrade pip \
    && pip install wheel \ 
    && pip install -r requirements.txt

CMD ["bash"]