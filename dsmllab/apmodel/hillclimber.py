
import subprocess
import os
import re
import random


def execute_java():
  
  # this method calls mason and then grabs the output

  try:
    s = subprocess.check_output("javac *.java ../utilities/*.java;java dsmllab.apmodel.Surveillance homog.params", shell = True,stderr=subprocess.STDOUT)
  except subprocess.CalledProcessError as e:
    raise RuntimeError("\n\n command '{}' return with error (code {}): {}".format(e.cmd, e.returncode, e.output))

  

  s =s.decode("utf-8")

  #print(s)

  s = s.splitlines()[-1]

  s =re.sub("[^\d\.]", "", s)
  #print("output = ", s)

  #s = s.rstrip('\n')

  return float(s)


def update_force_block(new_values):



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

  #print("the new block is " ,new_block)
  #print(type(new_block))

  return new_block

def random_soln():
  #print("Getting an initial random solution...")

  block1 = "dsmllab.apmodel.Particle { \nsubtype = 0\nnumber = 30 \ndiameter = 3\ncolor = { 230, 0, 126 }\nfriction = 0.5 \n} "
  block2 = "\n\ndsmllab.apmodel.NewtonianForceLaw {\nfromParticleName = dsmllab.apmodel.Particle\nfromParticleSubtype = 0\ntoParticleName = dsmllab.apmodel.Particle\ntoParticleSubtype = 0\neffectRange = 1000\narBoundary = 40\nG = 100\ndistancePower = 2\nmassPower = 1.0\n}"
  block3 = "\n\ndsmllab.apmodel.Particle {\nsubtype = 2\nnumber = 30\ndiameter = 3 \ncolor = { 230, 0, 126 }\nfriction = 0.5\n}"
  block4 = "\n\ndsmllab.apmodel.NewtonianForceLaw {\nfromParticleName = dsmllab.apmodel.Particle\nfromParticleSubtype = 2\ntoParticleName = dsmllab.apmodel.Particle\ntoParticleSubtype = 2\neffectRange = 1000 \narBoundary = 40\nG = 100\ndistancePower = 2\nmassPower = 1.0\n}"
  block5 = "\n\ndsmllab.apmodel.Particle {\nsubtype = 3\nnumber = 1\ndiameter = 10\ncolor = { 255, 255, 255 }\nfriction = .01\nmass = 60\nsetPosition =1.0\nsetx = 350\nsety = 210\n}" 
  block6 = "\n\ndsmllab.apmodel.Particle {\nsubtype = 1\nnumber = 1\ndiameter = 10\ncolor = { 255, 255, 255 }\nfriction = 1.0\nmass = 100\nsetPosition=1.0\nsetx = 150\nsety = 200\n}" 
  block7 = "\n\ndsmllab.apmodel.NewtonianForceLaw {\nfromParticleName = dsmllab.apmodel.Particle\nfromParticleSubtype = 3\ntoParticleName = dsmllab.apmodel.Particle\ntoParticleSubtype = 0\neffectRange = 350\narBoundary = 10\nG = 10\ndistancePower = 2.0\nmassPower = 1.0\n}"
  block8 = "\n\ndsmllab.apmodel.NewtonianForceLaw {\nfromParticleName = dsmllab.apmodel.Particle\nfromParticleSubtype = 1\ntoParticleName = dsmllab.apmodel.Particle\ntoParticleSubtype = 2\neffectRange = 350\narBoundary = 10\nG = 10\ndistancePower = 2.0\nmassPower = 1.0\n}"

  # Items in the list of solutions will correspond to different blocks in the .params file
  # First item: Block 2, 2nd item: block 4, 3rd item: 5th block, 4th item: 6th Block, 5th item: 7th Block,6th item: 8th Block 

  # At first, we'll just mess with one block

  soln = [block1, block2, block3, block4, block5, block6, block7, block8]

  block2_values = [1000,40,100,2,1.0]

  return soln,block2_values

def evaluate(soln):
  #print("Evaluating the solution...")

  # we need to start by first updating the .params file so that we know we have the updated values
  full_soln_string = ""
  for i in range(len(soln)):
    #print("this block = ", soln[i])
    full_soln_string += soln[i]

  with open('homog2.params', "w") as myfile:
    myfile.write(full_soln_string)

  # after updating the params file, call mason and grab the percentage of targets covered 
  score = execute_java()

  return score

def modify_soln(soln,block2_values):
  #print("Modifiying a solution...")

  # For now, we'll just modify block 2 
  block = soln[1]

  #print("old block values: ", block2_values)

  #for i in range(len(block2_values)):

  n_changes =1
  for i in range(n_changes):
    # select a value to change
    ix = random.randint(0, len(block2_values)-1)
    block2_values[ix] = block2_values[ix] + .5*block2_values[ix]

    
  # update block 2 

  #print("passing in ", block2_values)
  block2 = update_force_block(block2_values)
  # update the whole solution
  soln[1] = block2

  return soln,block2_values

def hill_climber(max_iterations):

  # Get an initial, random solution
  soln, block2_vals = random_soln()

  # evaluate the initial solution
  score = evaluate(soln)

  for i in range(max_iterations): 
    # generate a new candidate by modifying an initial solution
    candidate,block2_vals = modify_soln(soln,block2_vals)

    # evaluate the candidate
    cand_score = evaluate(candidate)

    print("cand score = ", cand_score)
    print("current score = ", score)

    if cand_score >= score:
      score = cand_score
      soln = candidate
      print("Best Score so far! \nSolution: ", soln, "\nScore: ",score)

    else: print("That was not better, keeping the old one")

  return soln,score
  
# Driver function
if __name__=="__main__":
    soln, score = hill_climber(10)

    #for i in range(len(soln)):
      #print(soln[i])

    #print("score: " , score)

    