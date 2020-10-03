library(readr)
library(tidyverse)
library(gdata)

file_path <- commandArgs(trailingOnly = TRUE)[1]

data <- read_csv(file_path)
data <- read_csv("depth_4.csv")
data <- data %>% filter(Depth >= 3) 

set.seed(3141592)
rows <- sample(nrow(data))
data <- data[rows, ]


