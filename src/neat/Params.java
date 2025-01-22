package neat;

public interface Params {

	int numInputs = 4;
	int numOutputs = 1;
	int FramesPerSecond =60;
	double MaxTurnRate =0.2;
	int SweeperScale =5;
	int NumSensors =5;
	double SensorRange =25;
	int NumBirds =150;
	int NumTicks =2000;
	double CellSize =20;
	int NumAddLinkAttempts =5;
	double SurvivalRate =0.2;
	int NumGensAllowedNoImprovement =7;
	int MaxPermittedNeurons =100;
	double ChanceAddLink =0.07;
	double ChanceAddNode=	0.03;
	double ChanceAddRecurrentLink =0.05;
	double MutationRate= 0.8;
	double MaxWeightPerturbation =0.5;
	double ProbabilityWeightReplaced= 0.1;
	double ActivationMutationRate =0.1;
	double MaxActivationPerturbation =0.1;
	double CompatibilityThreshold =0.26;
	int OldAgeThreshold =50;
	double OldAgePenalty =0.7;
	double YoungFitnessBonus =1.3;
	int YoungBonusAgeThreshhold =10;
	double CrossoverRate =0.7;
	
     
int NumTrysToFindLoopedLink     = 5;
int  NumTrysToFindOldLink   =5;
int NumBestBirds             = 4;
double SigmoidResponse          = 1;
double Bias                     = -1;

}
