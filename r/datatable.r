library(arrow)
library(data.table) 
setDTthreads(12)

main <- function () {
    start_time <- Sys.time()
    files <- Sys.glob('/data/performance-benchmark-data/*.parquet')
    dataframes <- lapply(files, function(x) { read_parquet(x) })
    dataframe <- do.call(rbind, dataframes)
    setDT(dataframe)
    dataframe[, sales := quantity * price]
    result = dataframe[, 
                    .(total_spend = sum(sales),
                      avg_basket_size = mean(sales),
                      avg_price = mean(price),
                      n_transactions = .N,
                      n_visits = uniqueN(date),
                      n_brands = uniqueN(`brand-id`),
                      n_styles = uniqueN(`style-id`)),
                    by = `member-id`]
    write_parquet(result, "datatable.parquet")
    end_time <- Sys.time()
    print(end_time - start_time)
}

main()

df <- read_parquet("datatable.parquet")
dim(df)
head(df)
colnames(df)


# 1 Part
# Time difference of 27.56955 secs

# 12 Parts
# Time difference of 2.378997 mins
