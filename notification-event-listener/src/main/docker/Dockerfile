FROM powerme/base:8144

COPY binlog-docker /
COPY maven /maven

ENV BIN_DIR=/opt/apps/product/notification/listener
ENV MNT_DIR=/opt/data
ENV JAVA_MAX_MEM_RATIO=80
ENV JAVA_APP_DIR=$BIN_DIR
ENV JAVA_APP_JAR=notification-event-listener-1.0.jar

RUN sed -i 's/\r$//g' binlog-docker \
    && mkdir -p $BIN_DIR \
    && cp -a /maven/* $BIN_DIR/ \
    && rm -rf /maven \
    && cd $BIN_DIR \
    && mkdir default-config \
    && cp -a config/* default-config/ \
    && mkdir -p $MNT_DIR/logs \
    && mv config $MNT_DIR/ \
    && mv logs $MNT_DIR/ \
    && ln -s $MNT_DIR/config config \
    && ln -s $MNT_DIR/logs logs \
    && chown powerme:powerme /binlog-docker \
    && chmod +x /binlog-docker \
    && chown -R powerme:powerme /opt/apps \
    && chown -R powerme:powerme $MNT_DIR

WORKDIR $BIN_DIR

ENTRYPOINT ["/binlog-docker"]
CMD ["run"]
