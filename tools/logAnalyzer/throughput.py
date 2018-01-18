"""
Copyright (c) 2017 original authors and authors.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
"""

import sys
import glob
import os
import re
import matplotlib
import numpy as np
import pylab as pl
import operator

"""Parse and return a log file.

Each valid line of the log are stored as an array as follow:
[back_pressure_status: bool, current_capacity: int, current_time: int]
back_pressure_status: The back pressure status of the consumer.
current_capacity: Current capacity of the inbound queue of the consumer.
current_time: Time of this line of log.

@param log_name Name of the log file.
@return return an 2D array that contains necessary informations.
"""
def parseLog(log_name):
    ret = []
    f = open(log_name, 'r')
    for line in f:
        if "Message per second:" in line:
            message_per_second = line.split("Message per second: ")[1]\
                            .split(" ")[0]
            current_time = int(line.split("Time: ")[1].split("\n")[0])
            ret.append([message_per_second, current_time])
    if len(ret) == 0:
        print "No valid line in log."
        sys.exit()
    return ret

"""
Remove lines in each log that are not intersected with other logs in time.
"""
def pruneAndResetOffsetLog(logs):
    minTime = logs[0][1][0][1]
    for log in logs:
        if minTime < log[1][0][1]:
            minTime = log[1][0][1]
    maxTime = logs[0][1][-1][1]
    for log in logs:
        if maxTime > log[-1][1]:
            maxTime = log[-1][1]
    for log in logs:
        log[1] = [line for line in log[1] if line[1] >= minTime]
        log[1] = [line for line in log[1] if line[1] <= maxTime]
        # Reset time offset
        for line in log[1]:
            line[1] = line[1] - minTime 

"""
Get data points from the log's lines
x axis: Time.
y axis: Current capacity of the inbound queue of the consumer.
"""
def getPoints(lines):
    x = []
    y = []
    for line in lines:
        x.append(line[1])
        y.append(line[0])
    return x, y

if __name__ == "__main__":
    # Change working directory to "../logs/"
    os.chdir("../logs")
    root_dir = os.path.dirname(os.path.abspath(__file__))
    flink_dir_pattern = re.compile("^flink")
    """
    logs store log files of different Flink consumers.
    Structure:
    [file_name, log]
    file_name: Name of the log file.
    log: The log that contains all lines of information of such file.
    """
    log_filename = ""
    if len(sys.argv) >= 2:
        log_filename = sys.argv[1].split("/")[-1]

    logs = []
    legend_dict = {}
    legend_dict['flink0'] = 'delay = 20ms'
    legend_dict['flink1'] = 'delay = 20ms'
    legend_dict['flink2'] = 'delay = 5ms'
    latency_config = "20-20-5"
    for _, dirs, _ in os.walk(root_dir):
        for directory in dirs:
            if flink_dir_pattern.match(directory):
                list_of_files = glob.glob(root_dir + '/' + directory + '/*')
		filepath = ""
                if log_filename == "":
                    filepath = max(list_of_files, key = os.path.getctime)
                    log_filename = filepath.split("/")[-1]
                else:
                    filepath = root_dir + '/' + directory + '/' + log_filename
		file_directory = filepath.split("/")[-2]
                logs.append([legend_dict[file_directory], parseLog(filepath)])
    pruneAndResetOffsetLog(logs)
    # Sort the logs by log's directory name.
    logs = sorted(logs, key = lambda logs: logs[0])

    # Change working directory back to "../logAnalyzer/"
    os.chdir("../logAnalyzer")

    # Plot
    for log in logs:
        x, y = getPoints(log[1])
        plot = pl.plot(x, y, label=log[0])
    pl.title('LSMD Throughput')
    pl.xlabel('Time(ms)')
    pl.ylabel('Messages Per Second')
    art = []
    lgd = pl.legend(loc='center right', bbox_to_anchor=(1.4, 0.5))
    art.append(lgd)
    pl.savefig('./results/' + log_filename.split(".")[0] + " " + latency_config\
             + " throughput", \
            additional_artists=art, bbox_inches = "tight")
