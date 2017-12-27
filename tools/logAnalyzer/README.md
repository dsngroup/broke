# Logger Analyzer

### Analyze the latest logs in all "flink\*" directories.
Plot log results with the latest log in all "flink\*" directories into a diagram and store in "results" directory
```shell
python parser.py
```
### Parse a specific log 
Plot log results specified by the argument 1's file name in all "flink\*" directories into a diagram and store in "results" directory
Argument: A log file in one of the "flink\*" directories.
```shell
python parser.py [flink*/log-XXX.txt]
```
