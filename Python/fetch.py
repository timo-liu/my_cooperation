import pandas as pd
import pickle
import os
import io
from tqdm import tqdm

#gets headers
def get_headers(lines):
    header_dicts = {}
    reading_headers = False
    current_counter = 0
    headed = ""
    for line in lines:
        if not reading_headers:
            if line.startswith('#'):
                reading_headers = True
                headed += line.strip('\n')
        else:
            if not line.startswith('#'):
                reading_headers = False
                header_dicts[current_counter] = headed
                current_counter += 1
                headed = ""
            else:
                headed += line.strip('\n')
    return header_dicts

# fetching functions
def fetch_all_data(prefix, group_data_time_steps, agent_data_time_steps):
    if not os.path.exists(f"revamped_{prefix}_backup"):
        os.mkdir(f"revamped_{prefix}_backup")
        os.mkdir(f"revamped_{prefix}_backup/Group_data")
        os.mkdir(f"revamped_{prefix}_backup/Agent_data")
    
    er = {}
    
    group_dfs = get_groups(prefix, group_data_time_steps, er)
    agent_dfs = get_agents(prefix, agent_data_time_steps, er)
    return er, group_dfs, agent_dfs

def get_groups(prefix, time_steps, existing_runs, start_from=0):
    group_dfs = {}

    if start_from == 0:
        pass
    else:
        with open(f'revamped_{prefix}_existing_runs_backup.pkl', 'rb') as f:
            existing_runs = pickle.load(f)
    
    group_directory = f"{prefix}/Group_data"
    group_backup_directory = f"revamped_{prefix}_backup/Group_data"
    
    for filename in tqdm(os.listdir(group_directory)[start_from:], desc = "Processing group files"):
        file_path = os.path.join(group_directory, filename)
        # Check if it's a regular file (not a directory)
        if os.path.isfile(file_path) and prefix in file_path:
            group_id = int(filename.split('-')[1].strip('.txt'))

            if group_id not in group_dfs:
                group_dfs[group_id] = {}
            
            with open(file_path, 'r') as temp:
                read = temp.readlines()
            temp_lines = [line for line in read if not line.startswith('#')]
            i2h = get_headers(read)
            num_sections = len(temp_lines)//(time_steps+1)
            df_set = {}
            for i in tqdm(range(num_sections), desc="Iterating through runs"):
                filtered_csv_string = ''.join(temp_lines)
                csv_io = io.StringIO(filtered_csv_string)
                df = pd.read_csv(csv_io, skiprows = i*(time_steps+1), nrows = time_steps, comment="#")
                # iterating through the file, so after getting this df, I need to store it in group_dfs, under its respective group id
                # group_dfs storage format will be group_dfs[{group id}][{header}] = array of runs in dataframe format that share the same header
                # i = index of header in i2h
                # existing runs is now just a dictionary of headers with counts for sanity checking
                # existing runs[{header}] = {'num_agent_runs': {count of agent runs that share a header}, 'num_group_runs':{count of group runs that share a header}}
                current_header = i2h[i]
                if current_header not in existing_runs:
                    existing_runs[current_header] = {'num_agent_runs': 0, 'num_group_runs':1}
                else:
                    existing_runs[current_header]['num_group_runs'] += 1
                # logic for handling addition of redundant runs
                if current_header not in group_dfs[group_id]:
                    group_dfs[group_id][current_header] = [df]
                else:
                    group_dfs[group_id][current_header].append(df)
            
            with open(f"{group_backup_directory}/{filename.strip('.txt')}.pkl", 'wb+') as file:
                pickle.dump(group_dfs[group_id], file)
    with open(f"revamped_{prefix}_existing_runs_backup.pkl", 'wb+') as file:
        pickle.dump(existing_runs, file)
    return group_dfs

def get_agents(prefix, time_steps, existing_runs, start_from=0):
    agent_dfs = {}
    agent_directory = f"{prefix}/Agent_data"
    agent_backup_directory = f"revamped_{prefix}_backup/Agent_data"

    if start_from == 0:
        pass
    else:
        with open(f'revamped_{prefix}_existing_runs_backup.pkl', 'rb') as f:
            existing_runs = pickle.load(f)
    
    for filename in tqdm(os.listdir(agent_directory)[start_from:], desc="Processing agent files"): # for each file in the os
        file_path = os.path.join(agent_directory, filename)
        # Check if it's a regular file (not a directory)
        if os.path.isfile(file_path) and prefix in file_path:

            agent_id = int(filename.split('-')[1].strip('.txt'))

            if agent_id not in agent_dfs:
                agent_dfs[agent_id] = {}
            with open(file_path, 'r') as temp:
                read = temp.readlines()
            temp_lines = [line for line in read if not line.startswith('#')] # get lines for reading pure dfs
            i2h = get_headers(read) # get i2h, h2i, seeds in order
            num_sections = len(temp_lines)//(time_steps+1)
            for i in tqdm(range(num_sections), desc="iterating through agent runs"): # for each RUN
                filtered_csv_string = ''.join(temp_lines)
                csv_io = io.StringIO(filtered_csv_string)
                df = pd.read_csv(csv_io, skiprows = i*(time_steps+1), nrows = time_steps, comment="#")
                # agent_dfs structure is going to be similar
                # agent_dfs[{agent_id}][{header}] = array of runs in dataframe format that share the same header
            
                
                current_header = i2h[i]
                if current_header not in existing_runs:
                    existing_runs[current_header] = {'num_agent_runs': 1, 'num_group_runs': 0}
                else:
                    existing_runs[current_header]['num_agent_runs'] += 1
                # logic for handling addition of redundant runs
                if current_header not in agent_dfs[agent_id]:
                    agent_dfs[agent_id][current_header] = [df]
                else:
                    agent_dfs[agent_id][current_header].append(df)
            with open(f"{agent_backup_directory}/{filename}.pkl", 'wb+') as file:
                pickle.dump(agent_dfs[agent_id], file)
    with open(f"revamped_{prefix}_existing_runs_backup.pkl", 'wb+') as file:
        pickle.dump(existing_runs, file)
    return agent_dfs

if __name__ == "__main__":
    _, _, _ = fetch_all_data('false_200', 200, 40)