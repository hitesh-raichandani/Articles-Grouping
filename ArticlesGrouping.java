import java.io.*;
import java.util.*;

public class ArticlesGrouping
{
    static int b = 20 ,r = 5;
    static List<List<Integer>> shingles = new ArrayList();
    static TreeSet<Integer> universalSet = new TreeSet();
    static List<String> fileList = new ArrayList();
    static byte mappingMatrix[][];
    static int signatureMatrix[][];
    static double threshold = Math.pow((1.0/b), (1.0/r));
    static int similarityThreshold = 60;
    
    static void getInput() throws Exception
    {
        String input, fileName, line, tokens[];
        Scanner sc = new Scanner(System.in);
        String defaultPath = "/home/mwang2/test/coen281/";
        
        while(sc.hasNextLine())
        {
            fileName = sc.nextLine();
            fileName = fileName.trim();
            fileName = fileName.replaceAll("\\t+", " ");
            fileName = fileName.replaceAll("\\s+", " ");
            if(fileName.equals(""))
                break;
            tokens = fileName.split(" ");

            for(String token : tokens)
            {
                if(token.contains("/"))
                {

                }
//                    else
//                        token = defaultPath + token;

                File file = new File(token);
                FileInputStream fis = new FileInputStream(file);
                byte[] data = new byte[(int) file.length()];
                fis.read(data);
                fis.close();
                String content = new String(data);
                content = content.trim();
                content = content.replaceAll("\\t"," ");

                //making shingles
                List<Integer> fileShingles = new ArrayList();
                for(int i = 0; i < content.length() - 8; i++)
                {
                    int temp = content.substring(i, i + 9).hashCode();
                    fileShingles.add(temp);
                    universalSet.add(temp);
                }
                shingles.add(fileShingles);
                fileList.add(token);
            }
        }
    }
    
    // Create Mapping Matrix
    static void mappingMatrix()
    {
        mappingMatrix = new byte[fileList.size()][universalSet.size()];
        for(int i = 0; i < fileList.size(); i++)
        {
            int j = 0;
            for(int shing : universalSet)
            {
                if (shingles.get(i).contains(shing))
                {
                    mappingMatrix[i][j] = 1;
                }
                else
                    mappingMatrix[i][j] = 0;
                j++;
            }
        }
    }
    
    // Creating Signature Matrix
    static void signatureMatrix()
    {
        signatureMatrix = new int[fileList.size()][b*r];
        LinkedHashMap<Integer, Integer> temp = new LinkedHashMap();
        for( int i = 0, c = 23; i < b * r; i++)
        {
            for(int j = 0; j < universalSet.size(); j++)
            {
                temp.put((j + c) % universalSet.size(), j);
            }

            for(int j = 0; j < fileList.size(); j++)
            {
                for(int k = 0; k < universalSet.size(); k++)
                {
                    if(mappingMatrix[j][temp.get(k)] == 1)
                    {
                        signatureMatrix[j][i] = temp.get(k);
                        break;
                    }
                }
            }
            c += 16;
        }
    }
    
    @SuppressWarnings("main")
    public static void main(String[] args)
    {
        try
        {
            getInput();
            mappingMatrix();
            signatureMatrix();
            StringBuilder finalPairs = null;
            //create pairs in each band
            StringBuilder bandPairs = new StringBuilder();
            for(int j = 0; j < signatureMatrix[0].length - r; j += r)
            {
                for(int i = 0; i < signatureMatrix.length; i++)
                {
                    
                    int temp1 = 0;
                    for(int k = j; k < j + r; k++)
                    {
                        temp1 *= 10;
                        temp1 += signatureMatrix[i][k];
                    }
                    bandPairs.append(temp1 + ",");
                }
                
                bandPairs.setLength(bandPairs.length() - 1);
                bandPairs.append("\n");
            }
            bandPairs.setLength(bandPairs.length() - 1);
            
            // Making CandidatePairs
            StringBuilder candidatePairs = new StringBuilder();
            String pairs = bandPairs.toString();
            String rows[] = pairs.split("\n");
            for(int i = 0; i < rows.length; i++)
            {
                String cols[] = rows[i].split(",");
                for(int j = 0; j < (cols.length - 1); j++)
                {
                    for(int k = j + 1; k < cols.length; k++)
                    {
                        if(cols[j].equals(cols[k]) && j != k)
                        {
                            String t = candidatePairs.toString();
                            if (t.contains(j + "," + k))
                            {}
                            else
                                candidatePairs.append(j + "," + k + "\n");
                        }
                    }
                }
            }
            if(candidatePairs.length() > 0)
                candidatePairs.setLength(candidatePairs.length() - 1);
            else
                candidatePairs = null;
            
            //continue if candidate pairs are found
            if(candidatePairs != null)
            {
                //removing false positives
                String t = candidatePairs.toString();
                StringBuilder candidatePairsNew = new StringBuilder();
                String row[] = t.split("\n");
                for(int i = 0; i < row.length; i++)
                {
                    int similar = 0;
                    String col[] = row[i].split(",");
                    for(int j = 0; j < signatureMatrix[0].length; j++)
                    {
                        if(signatureMatrix[Integer.parseInt(col[0])][j] == signatureMatrix[Integer.parseInt(col[1])][j])
                        {
                            similar++;
                        }
                    }
                    if((similar/(b*r)) > threshold)
                    {
                        candidatePairsNew.append(col[0] + "," + col[1] + "\n");
                    }
                }
                
                if(candidatePairsNew.length() > 0)
                {
                }
                else
                    candidatePairsNew = null;

                //continue if there are pairs after removing false positives
                if(candidatePairsNew != null)
                {
                    //finding similarity from universal set
                    String t1 = candidatePairsNew.toString();
                    finalPairs = new StringBuilder();
                    String rowz[] = t1.split("\n");

                    for(int i = 0; i < rowz.length; i++)
                    {
                        int total = 0, similar = 0;
                        String colz[] = rowz[i].split(",");
                        int f1 = Integer.parseInt(colz[0]);
                        int f2 = Integer.parseInt(colz[1]);
                        for(int j = 0; j < mappingMatrix[0].length; j++)
                        {
                            if(mappingMatrix[f1][j] == 1 && mappingMatrix[f2][j] == 1)
                            {
                                similar++;
                                total++;
                            }
                            else if( (mappingMatrix[f1][j] == 1 && mappingMatrix[f2][j] == 0) || (mappingMatrix[f1][j] == 0 && mappingMatrix[f2][j] == 1) )
                            {
                                total++;
                            }
                        }
                        if( ((similar/total) * 100) > similarityThreshold)
                            finalPairs.append(colz[0] + "," + colz[1] + "\n");
                    }
                }
            }
            
            //Display
            if(candidatePairs == null)
            {
                for(int i = 0; i < fileList.size(); i++)
                {
                    System.out.println("Group " + (i + 1) + ": " + fileList.get(i));
                }
            }
            else
            {
                StringBuilder display = new StringBuilder();
                String pair = finalPairs.toString();
                String row[] = pair.split("\n");
                for(int i = 0; i < row.length; i++)
                {
                    String col[] = row[i].split(",");
                    if(display.indexOf((col[0]) + " ") == -1)
                    {
                        if(display.indexOf((col[1]) + " ") == -1)
                        {
                            display.append("\n" + col[0] + " " + col[1]);
                        }
                        else
                        {
                            if(display.indexOf((col[1]) + " ") == -1)
                                display.insert(display.indexOf(" ", display.indexOf(col[1] + " ")), " " + (col[0] + " "));
                        }
                    }
                    else
                    {
                        if(display.indexOf((col[1]) + " ") == -1)
                        {
                            display.insert(display.indexOf(" ", display.indexOf(col[0] + " ")), " " + (col[1] + " "));
                        }
                    }
                }
                
                String d = display.toString();
                d = d.trim();
                row = d.split("\n");
                boolean displayed[] = new boolean[fileList.size()];
                int g = 1;
                for(int i = 0; i < fileList.size(); i++)
                {
                    for(int j = 0; j < row.length; j++)
                    {
                        row[j] = row[j].replaceAll("\\s+", " ");
                        String col[] = row[j].split(" ");
                        System.out.print("Group " + g + ": ");
                        for(int k = 0; k < col.length - 1; k++, i++)
                        {
                            System.out.print(fileList.get(Integer.parseInt(col[k])) + ", ");
                            displayed[Integer.parseInt(col[k])] = true;
                        }
                        System.out.print(fileList.get(Integer.parseInt(col[col.length - 1])));
                        System.out.println();
                        displayed[Integer.parseInt(col[col.length - 1])] = true;
                        g++;
                        i++;
                    }
                    for(int j = 0; j < fileList.size(); j++)
                    {
                        if(displayed[j] == false)
                        {
                            System.out.println("Group " + g + ": " + fileList.get(j));
                            g++;
                            i++;
                        }
                    }
                }
            }
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }
}