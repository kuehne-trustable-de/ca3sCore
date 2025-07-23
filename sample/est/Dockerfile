FROM ubuntu:18.04

RUN apt update && apt install -y apache2-utils git openssl libssl-dev build-essential && \
    git clone https://github.com/cisco/libest.git && cd libest && \
    ./configure --disable-safec --disable-shared --enable-static && make install && \
    rm -rf /src && apt remove --quiet -y libssl-dev build-essential && \
    apt autoremove -y && apt clean -y && apt autoclean -y && \
    rm -rf /var/lib/apt /tmp/* /var/tmp/*

WORKDIR /libest/example/server/

# setting the root CA for 20 years
RUN sed -i "s|-days 365|-days 7300 |g" ./createCA.sh

# this will create the EST server certificate with our real external name
### Change the next line ###
RUN sed -i "s|ip6-localhost|estserver.saye.org |g" ./ext.cnf

# this will make our EST server certificate good for 10 years
RUN sed -i "s|-keyout \$EST_SERVER_PRIVKEY -subj|-keyout \$EST_SERVER_PRIVKEY -days 7300 -subj |g" ./createCA.sh

# creating the certificates
RUN echo 1 | ./createCA.sh

# defining a none default userid and password to auth to the est server
RUN htpasswd -cb /libest/example/server/estCA/estpwdfile estcertuser May2021

# OPTIONAL changing our cert default-days to 1 to show EST renewal
# RUN sed -i "s|default_days   = 365|default_days   = 1 |g" ./estExampleCA.cnf

# changing the port to be TCP 443
RUN sed -i "s|estserver -c|estserver -p 443 -c |g" ./runserver.sh
EXPOSE 443
CMD ./runserver.sh
