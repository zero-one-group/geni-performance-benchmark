FROM julia:1.5.1-buster

RUN julia -e 'using Pkg; Pkg.add("DataFrames")'

RUN julia -e 'using Pkg; Pkg.add("ParquetFiles")'

RUN julia -e 'using ParquetFiles, DataFrames'

RUN julia -e 'using Pkg; Pkg.add("Queryverse")'
RUN julia -e 'using Queryverse'

RUN julia -e 'using Pkg; Pkg.add("Glob")'
RUN julia -e 'using Glob'
