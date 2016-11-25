%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Neuron2017.m
%
% Shows examples how to use java library and
% reproduce results from "Memory retrieval form first principles"
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%% setup java
clc
clear java
% here we adding a reference to jar file with byte code, so java classes
% will accessible in matlab
javaaddpath freeRecall/dist/freeRecall.jar
clf
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% set parameters
f= 0.01;         % sparcity of the network
nWords= 1638;    % number of words in a pool.
listSizes= 16* (1:8); % the number of words in a study list for different simulated experiments

nTrials= 10000;  % number of trials in a session
nSessions= 100;  % number of session to run


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% initializing network

% we define here that the actuall trial will be basic version of the model
% as described in the paper
tr= freeRecall.DeterministicTrial();

% we will perform simulations with by a sessions and all simulations are
% baesd on a pool of nWords
ses= freeRecall.Session( nWords );
% the simulation will be peformed using basic model transitions by the
% class defined above
ses.setTrialSim(tr);

% for the purpose of this simulation we will store the following
% information:
ses.stat.storeWordsP= true;        % list of presented words
ses.stat.computeRecallP= true;     % compute the recall probabilities in java
ses.stat.storeTransitionsP= true;  % recalled sequence
% we will not store to save space
ses.stat.storeTransitionMatrixP= false; % the computed transition matrices

%overall statistics across list sizes

% average number of words recalled for a given study list size
Nwr= nan(numel(listSizes),1);


% we need a similarity matrix for a whole pool of words
disp( 'computing similarity matrix ...' ) % this is lengthly process

sim= zeros( nWords);
tic
for k=1:10,
    pat= double(rand([10000, nWords])<f);
    sim= sim+ pat'*pat;
    toc
end
disp('done')

% communicate computed similarity matrix to session
ses.setSimilarityMatrix( sim );

% computing representation size
wordSize= diag( sim );

clear pat sim

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% simulations for different list sizes

for li= 1:numel(listSizes),
    listSize= listSizes(li);
    
    % we want java to allocate enough space for simulation
    ses.setListSize(listSize);   % in session
    tr.setListSize( listSize );  % and in trial
    
    %set up statistical measures
    nRecHist= zeros(listSize,1);  % histogram of number of words recalled
    nRec= zeros(nWords,1);        % number of times word is recalled
    nPres= zeros(nWords,1);       % number of times word presented
    Nwr1 =0; % accumulator for the average number of words recalled
    nRecPos= zeros(nWords,1);     % sum of recall positions, when recalled
    pTrRec= nan( nTrials,1);         % average recal probability of recalled words in a trial
    corrPNwr= nan(nSessions, 2);    % correlation measure
    
    for sesNum=1:nSessions,
        % run session
        ses.computeSession( nTrials );
        
        % extract stat
        wordsPres= double(ses.stat.presentedWords+1); % list of presented words
        wordsRec= double(ses.stat.recalledWords);          % list of recalled words
        reqSeq= double(ses.stat.transitions+1);              % recalled sequence
        nRecalled= double(ses.stat.nRecalled);             % number of recalled words
        
        nRecHist= nRecHist+ double(ses.stat.nRecHist);   % update histogram of recall statistics
        nRec= nRec+hist(wordsRec(wordsRec>0), 1:nWords)'; % update histogram of recalls across words
        nPres= nPres+ hist(wordsPres(:), 1:nWords)';      % update historgam of word presentations
        pRec= nRec./nPres;  % current estimation of probability of recall
        Nwr1= Nwr1+ mean(nRecalled); % update the average recall probability
        
        % compute word positions and correlations
        for nTr=1:nTrials,
            reqSeq1= reqSeq(nTr,:);
            reqSeq1( find( reqSeq1==0, 1):end )=[];
            [~, b] =  unique( reqSeq1, 'first' );
            reqSeq1 = reqSeq1(sort(b));
            wordSeq= wordsPres(nTr, reqSeq1);
            nRecPos(wordSeq)= nRecPos(wordSeq)+ (1:numel(wordSeq))';
            
            %estimation of correlations from one session
            % we will form the histoggram of this values when plotting
            pTrRec(nTr)= mean(pRec(wordSeq));
        end
        % average recall probability of presented words
        pTrPres= mean( pRec( wordsPres ), 2 ); 
        %corelation of number of recalled words in a trial with average
        %recall probability for presented words
        corrPNwr(sesNum,1)= corr2( nRecalled, pTrPres );
        %corelation of number of recalled words in a trial with average
        %recall probability for recalled words
        corrPNwr(sesNum, 2)= corr2( nRecalled, pTrRec );
        
        %plotting intermediate results
        subplot(321)
        plot(nRecHist, '.-k')
        xlim([0 listSize]+.5)
        set(gca, 'FontSize', 14)
        title 'distribution of trials across number of words recalled'
        xlabel 'number of recalled words in a trial, #'
        ylabel 'number of trials, #'
        subplot(322)
        plot(wordSize, pRec, '.k' );
        set(gca, 'FontSize', 14)
        title 'dependency of recall probability on the size of word representation'
        xlabel 'number of neurons encoding the word, #'
        ylabel( {'probability to recall word','when it is presented'});
        subplot(323)
        plot( wordSize, nRecPos./nRec, '.' )
        set(gca, 'FontSize', 14)
        title 'Easy words appear earlier in recall sequence'
        xlabel 'number of neurons encoding the word'
        ylabel( {'evarage position of','word in recall sequence'})
        subplot(324)
        [hh, hx]=hist( corrPNwr, 40 );
        hb= bar(hx, hh);
        set(hb(2), 'FaceColor', 'r')
        set(hb(1), 'FaceColor', 'b')
        set(gca, 'FontSize', 14)
        title( {'correlations between number of words recalled and',...
          'average probability of (blue) presented words; (red) recalled words' });
      xlabel 'correlation coefficient'
      ylabel 'number of sessions'
        subplot(325)
        plot( nRecalled, pTrRec, '.' )
        set(gca, 'FontSize', 14)
        xlabel 'number of words recalled in a trial'
        ylabel( {'average recall probability','of recalled words'});
        drawnow
        toc
    end
    
    Nwr(li)= Nwr1/nSessions;
    subplot(326)
    if li>2,
        x= linspace(16, 128, 1e4);
        pf= polyfit( log( listSizes(1:li)), log(Nwr(1:li))', 1);
        plot( listSizes, Nwr, '.-k', x, exp(polyval(pf, log(x))), '-r' );
        set(gca, 'FontSize', 14)
        text( 30,10, sprintf('power fit: N_{wr}= %.2f LL^{%.3f}', exp(pf(2)), pf(1)))
    else
        plot( listSizes, Nwr, '.-k');
        set(gca, 'FontSize', 14)
    end
    title 'scaling law'
    xlabel 'study list size, LL'
    ylabel 'mean number of words recalled, N_{wr}'
end

%% meaningfullness

% On average the size of the overlap in random encoding model depens on the
% size of the representation in order to remove it we are going to change
% representation such that the size of representation is exactly the same
% for each word, whereas overlaps are defined by the random distribution of
% active neurons
f=0.01;
nn= round(f*1e4); % size of representation
pat= zeros([10000, nWords]);
sim= zeros( nWords);

for k=1:10,
    [~, ii]= sort(rand([10000, nWords]));
    [jj]= sub2ind([10000, nWords], ii(1:nn, :), repmat( 1:1638, nn, 1 ) );
    pat(:)=0;
    pat(jj)=1;
    sim= sim+ pat'*pat;
    toc
end
clear ii pat
sim= sim+ randn(size(sim))*1e-4;
overlaps= sum(sim)'-diag(sim);
wordSize= diag( sim );
ses.setSimilarityMatrix( sim );

figure
listSize=16;

% we want java to allocate enough space for simulation
ses.setListSize(listSize);   % in session
tr.setListSize( listSize );  % and in trial

ses.computeSession( nTrials );
%set up statistical measures
nRecHist= zeros(listSize,1);  % histogram of number of words recalled
nRec= zeros(nWords,1);        % number of times word is recalled
nPres= zeros(nWords,1);       % number of times word presented
Nwr1 =0; % accumulator for the average number of words recalled
nRecPos= zeros(nWords,1);     % sum of recall positions, when recalled
pTrPres= nan( nTrials,1);         % average recal probability of presented words in a trial
pTrRec= nan( nTrials,1);         % average recal probability of recalled words in a trial
corrPNwr= nan(nSessions, 2);    % correlation measure

for sesNum=1:nSessions,
    % run session
    ses.computeSession( nTrials );
    
    % extract stat
    wordsPres= double(ses.stat.presentedWords+1); % list of presented words
    wordsRec= double(ses.stat.recalledWords);          % list of recalled words
    reqSeq= double(ses.stat.transitions+1);              % recalled sequence
    nRecalled= double(ses.stat.nRecalled);             % number of recalled words
    
    nRecHist= nRecHist+ double(ses.stat.nRecHist);
    nRec= nRec+hist(wordsRec(wordsRec>0), 1:nWords)';
    nPres= nPres+ hist(wordsPres(:), 1:nWords)';
    pRec= nRec./nPres;  % current estimation of probability of recall
    Nwr1= Nwr1+ mean(nRecalled);
    
    % compute word positions and correlations
    for nTr=1:nTrials,
        reqSeq1= reqSeq(nTr,:);
        reqSeq1( find( reqSeq1==0, 1):end )=[];
        [~, b] =  unique( reqSeq1, 'first' );
        reqSeq1 = reqSeq1(sort(b));
        wordSeq= wordsPres(nTr, reqSeq1);
        nRecPos(wordSeq)= nRecPos(wordSeq)+ (1:numel(wordSeq))';
        
        %crude estimation of correlations from one session
        % increasing number of trials in a session gives less
        % estimation error
        pTrRec(nTr)= mean(pRec(wordSeq));
    end
    pTrPres= mean( pRec( wordsPres ), 2 );
    corrPNwr(sesNum,1)= corr2( nRecalled, pTrPres );
    corrPNwr(sesNum, 2)= corr2( nRecalled, pTrRec );
    %plotting intermediate results
    subplot(321)
    plot(nRecHist, '.-k')
    xlim([0 listSize]+.5)
        set(gca, 'FontSize', 14)
        title 'distribution of trials across number of words recalled'
        xlabel 'number of recalled words in a trial, #'
        ylabel 'number of trials, #'
    subplot(322)
    plot(wordSize, pRec, '.k' );
        set(gca, 'FontSize', 14)
        title 'dependency of recall probability on the size of word representation'
        xlabel 'number of neurons encoding the word, #'
        ylabel( {'probability to recall word','when it is presented'});
    subplot(323)
    plot( wordSize, nRecPos./nRec, '.' )
        set(gca, 'FontSize', 14)
        title 'Easy words appear earlier in recall sequence'
        xlabel 'number of neurons encoding the word'
        ylabel( {'evarage position of','word in recall sequence'})
     subplot(324)      
        [hh, hx]=hist( corrPNwr, 40 );
        hb= bar(hx, hh);
        set(hb(2), 'FaceColor', 'r')
        set(hb(1), 'FaceColor', 'b')
        set(gca, 'FontSize', 14)
        title( {'correlations between number of words recalled and',...
          'average probability of (blue) presented words; (red) recalled words' });
      xlabel 'correlation coefficient'
      ylabel 'number of sessions'
    subplot(325)
    plot( overlaps, pRec, '.' )
        set(gca, 'FontSize', 14)
    xlabel 'cumulative overlap size'
        ylabel( {'probability to recall word','when it is presented'});
    drawnow
    toc
    
end