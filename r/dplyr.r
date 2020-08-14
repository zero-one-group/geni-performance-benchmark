library(arrow)
library(dplyr)

main <- function () {
    start_time <- Sys.time()
    files <- Sys.glob('/data/performance-benchmark-data/*.parquet')
    dataframes <- lapply(files[1:1], function(x) { read_parquet(x) })
    dataframe <- bind_rows(dataframes)
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
        write_parquet("dplyr.parquet")
    end_time <- Sys.time()
    print(end_time - start_time)
}

main()

df <- read_parquet("dplyr.parquet")
print(df)
head(df)
colnames(df)

# 1 Part
# Time difference of 7.683979 mins
# Without Docker
# Time difference of 7.343145 mins

# 12 Parts
# Time difference of 16.54721 mins
# Without Docker
# Time difference of 15.86956 mins
