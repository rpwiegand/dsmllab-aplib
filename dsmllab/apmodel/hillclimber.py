
import subprocess
import os
import re
import random
import copy

setting ="het"

def execute_java():
  
  # this method calls mason and then grabs the output

  try:
    s = subprocess.check_output("javac *.java ../utilities/*.java;java dsmllab.apmodel.Surveillance homog.params", shell = True,stderr=subprocess.STDOUT)
  except subprocess.CalledProcessError as e:
    raise RuntimeError("\n\n command '{}' return with error (code {}): {}".format(e.cmd, e.returncode, e.output))

  s =s.decode("utf-8")
  s = s.splitlines()[-1]
  s =re.sub("[^\d\.]", "", s)
  #s = s.rstrip('\n')

  return float(s)


def update_block(block_num, new_values):
  new_block =""

  if block_num==1: 
    new_block = "\n\ndsmllab.apmodel.Particle {"\
    "\nsubtype = 0" \
    "\nnumber = 30 "\
    "\ndiameter = 3"\
    "\ncolor = { 230, 0, 126 }"\
    "\nfriction = " + str(new_values[0]) + "\n}"

  elif block_num==3:
    if setting == "homog": color = "{ 230, 0, 126 }"
    else: color = "{ 0, 170, 0 }"
    new_block = "\n\ndsmllab.apmodel.Particle {"\
    "\nsubtype = 2" \
    "\nnumber = 30 "\
    "\ndiameter = 3"\
    "\ncolor = " + color + \
    "\nfriction = " + str(new_values[0]) + "\n}"

  elif block_num==5:
    
    new_block = "\n\ndsmllab.apmodel.Particle {"\
    "\nsubtype = 3" \
    "\nnumber = 1 "\
    "\ndiameter = 10"\
    "\ncolor = { 255, 255, 255 }"\
    "\nfriction = " + str(new_values[0]) + \
    "\nmass = " + str(new_values[1]) + \
    "\nsetPosition = 1.0" \
    "\nsetx = " + str(new_values[2]) + \
    "\nsety = " + str(new_values[3]) + "\n}"

  elif block_num==6:
    new_block = "\n\ndsmllab.apmodel.Particle {"\
    "\nsubtype = 1" \
    "\nnumber = 1 "\
    "\ndiameter = 10"\
    "\ncolor = { 255, 255, 255 }"\
    "\nfriction = " + str(new_values[0]) + \
    "\nmass = " + str(new_values[1]) + \
    "\nsetPosition = 1.0" \
    "\nsetx = " + str(new_values[2]) + \
    "\nsety = " + str(new_values[3]) + "\n}"

  if block_num==2:
    new_block = "\n\ndsmllab.apmodel.NewtonianForceLaw {"\
    "\nfromParticleName = dsmllab.apmodel.Particle" \
    "\nfromParticleSubtype = 0"\
    "\ntoParticleName = dsmllab.apmodel.Particle"\
    "\ntoParticleSubtype = 0"\
    "\neffectRange = " + str(new_values[0]) + \
    "\narBoundary = "+ str(new_values[1])+ \
    "\nG = " + str(new_values[2])+\
    "\ndistancePower = " + str(new_values[3]) + \
    "\nmassPower = " + str(new_values[4]) + "\n}"

  elif block_num ==4:
    new_block = "\n\ndsmllab.apmodel.NewtonianForceLaw {"\
    "\nfromParticleName = dsmllab.apmodel.Particle" \
    "\nfromParticleSubtype = 2"\
    "\ntoParticleName = dsmllab.apmodel.Particle"\
    "\ntoParticleSubtype = 2"\
    "\neffectRange = " + str(new_values[0]) + \
    "\narBoundary = "+ str(new_values[1])+ \
    "\nG = " + str(new_values[2])+\
    "\ndistancePower = " + str(new_values[3]) + \
    "\nmassPower = " + str(new_values[4]) + "\n}"    

  elif block_num ==7:
    new_block = "\n\ndsmllab.apmodel.NewtonianForceLaw {"\
    "\nfromParticleName = dsmllab.apmodel.Particle" \
    "\nfromParticleSubtype = 3"\
    "\ntoParticleName = dsmllab.apmodel.Particle"\
    "\ntoParticleSubtype = 0"\
    "\neffectRange = " + str(new_values[0]) + \
    "\narBoundary = "+ str(new_values[1])+ \
    "\nG = " + str(new_values[2])+\
    "\ndistancePower = " + str(new_values[3]) + \
    "\nmassPower = " + str(new_values[4]) + "\n}"    

  elif block_num ==8:
    new_block = "\n\ndsmllab.apmodel.NewtonianForceLaw {"\
    "\nfromParticleName = dsmllab.apmodel.Particle" \
    "\nfromParticleSubtype = 1"\
    "\ntoParticleName = dsmllab.apmodel.Particle"\
    "\ntoParticleSubtype = 2"\
    "\neffectRange = " + str(new_values[0]) + \
    "\narBoundary = "+ str(new_values[1])+ \
    "\nG = " + str(new_values[2])+\
    "\ndistancePower = " + str(new_values[3]) + \
    "\nmassPower = " + str(new_values[4]) + "\n}"    


  return new_block




def random_soln():
  #print("Getting an initial random solution...")
  # Items in the list of solutions will correspond to different blocks in the .params file
  # First item: Block 2, 2nd item: block 4, 3rd item: 5th block, 4th item: 6th Block, 5th item: 7th Block,6th item: 8th Block 


  block1_values = [0.5]  
  block3_values = [0.5]
  block5_values = [1.0,100,120,170]  
  block6_values = [1.0,100,350,210]  
  block2_values = [1000,40,100,2,1.0]
  block4_values = [1000,40,100,2,1.0]
  block7_values = [350,10,10,2.0,1.0]
  block8_values = [350,10,10,2.0,1.0]

  all_block_values = [block1_values, 
                      block2_values,
                      block3_values,
                      block4_values,
                      block5_values,
                      block6_values,
                      block7_values,
                      block8_values,
                      ]

  block1 = update_block(1,block1_values)
  block2 = update_block(2,block2_values)
  block3 = update_block(3,block3_values)
  block4 = update_block(4,block4_values)
  block5 = update_block(5,block5_values)
  block6 = update_block(6,block6_values)
  block7 = update_block(7,block7_values)
  block8 = update_block(8,block8_values)

  soln = [block1, block2, block3, block4, block5, block6, block7, block8]


  return soln,all_block_values

def update_file(soln):
  full_soln_string = ""
  for i in range(len(soln)):
    #print("this block = ", soln[i])
    full_soln_string += soln[i]

  with open('homog2.params', "w") as myfile:
    myfile.write(full_soln_string)

def evaluate(soln):
  #print("Evaluating the solution...")

  # we need to start by first updating the .params file so that we know we have the updated values
  update_file(soln)

  # after updating the params file, call mason and grab the percentage of targets covered 
  score = execute_java()

  return score

def modify_soln(soln,all_block_values):
  #print("Modifiying a solution...")

  vals = all_block_values

  #print("old block values: ", block2_values)

  #for i in range(len(block2_values)):

  n_changes =1
  for i in range(len(vals)):
    this_block_vals = vals[i]
    if i != 0 and i != 2:
      for j in range(n_changes):
        # select a value to change
        ix = random.randint(0, len(this_block_vals)-1)
        this_block_vals[ix] = this_block_vals[ix] + .1*this_block_vals[ix]

    vals[i] = this_block_vals

    block = update_block(i+1,this_block_vals)
    # update the whole solution
    soln[i] = block

  return soln,vals

def hill_climber(max_iterations):

  all_the_best_scores = []
  all_the_cand_scores =[]

  # Get an initial, random solution
  soln, all_block_values = random_soln()
  best_block_vals = all_block_values
  best = copy.deepcopy(best_block_vals)
  best_soln = soln

  # evaluate the initial solution
  score = evaluate(soln)

  all_the_best_scores = [soln]
  all_the_cand_scores =[score]

  for i in range(max_iterations): 

    best_block_vals = [x[:] for x in best]  # Copy the sublists in a.

    # generate a new candidate by modifying an initial solution
    candidate,these_vals = modify_soln(soln,best_block_vals)

    # evaluate the candidate
    cand_score = evaluate(candidate)

    print("cand score = ", cand_score)
    print("current score = ", score)

    if cand_score >= score:
      score = cand_score
      soln = candidate
      
      best = copy.deepcopy(these_vals)
      print("Best Score so far! \nSolution: ", soln, "\nScore: ",score)

    else: 
      print("That was not better, keeping the old one")
      update_file(soln)

    all_the_best_scores.append(score)
    all_the_cand_scores.append(cand_score)

  print("Best scores: \n", all_the_best_scores)
  print("Each cand score: \n", all_the_cand_scores)

  return soln,score
  
# Driver function
if __name__=="__main__":
    soln, score = hill_climber(50)

    #for i in range(len(soln)):
      #print(soln[i])

    #print("score: " , score)

    