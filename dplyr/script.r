library(arrow)
library(dplyr)

main <- function () {
    files <- Sys.glob('/data/performance-benchmark-data/*.parquet')
    dataframes <- lapply(files, function(x) { read_parquet(x) })
    dataframe <- bind_rows(dataframes)
    start_time <- Sys.time()
    dataframe %>%
        mutate(sales = price * quantity) %>%
        group_by(`member-id`) %>%
        summarise(total_spend = sum(sales),
                avg_basket_size = mean(sales),
                avg_price = mean(price),
                n_transactions = n(),
                n_visits = n_distinct(date),
                n_brands = n_distinct(`brand-id`),
                n_styles = n_distinct(`style-id`)) %>%
        write_parquet("final.parquet")
    end_time <- Sys.time()
    print(end_time - start_time)
}

main()

df <- read_parquet("final.parquet")
print(df)

# 1 Part
# Time difference of 7.683979 mins

# 12 Parts
# Time difference of 16.54721 mins
