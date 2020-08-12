FROM continuumio/miniconda:4.7.12

RUN apt update && apt install -y build-essential

RUN apt update && apt install -y r-base

RUN conda install -c conda-forge r-arrow

RUN R -e "install.packages(\"arrow\", repos=\"http://cran.us.r-project.org\")"
RUN R -e "arrow::install_arrow()"

RUN R -e "install.packages(\"stringr\", repos=\"http://cran.us.r-project.org\")"

RUN R -e "install.packages(\"dplyr\", repos=\"http://cran.us.r-project.org\")"

CMD ["R"]
