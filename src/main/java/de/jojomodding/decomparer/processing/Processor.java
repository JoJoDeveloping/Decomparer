package de.jojomodding.decomparer.processing;

import de.jojomodding.decomparer.config.Configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Processor {

    private Configuration config;

    public Processor(Configuration c){
        this.config = c;
    }

    public DifferentialFileTree process() throws IOException {
        File leftResult = new File(config.getTempDir(), "leftResult"), rightResult = new File(config.getTempDir(), "rightResult");
        if(!config.skipsDecompilation()) {
            DecompilerTask left = new DecompilerTask(config.getLeftSide(), leftResult), right = null;
            if (config.isParallel())
                right = new DecompilerTask(config.getRightSide(), rightResult);
            if (left.waitFor() != 0)
                throw new IOException("Left decompiler returned non-null!");
            if (!config.isParallel())
                right = new DecompilerTask(config.getRightSide(), rightResult);
            if (right.waitFor() != 0)
                throw new IOException("Right decompiler returned non-null!");
        }
        OrdinaryFileTree lft = buildOFT(new ZipFile(new File(leftResult, config.getLeftSide().getSource().getName()))), rft = buildOFT(new ZipFile(new File(rightResult, config.getRightSide().getSource().getName())));
        return buildDFT(lft, rft);
    }

    private OrdinaryFileTree buildOFT(ZipFile zf) throws IOException {
        OrdinaryFileTree root = new OrdinaryFileTree("/");
        for(ZipEntry ze : new Iterable<ZipEntry>() {
            @Override
            public Iterator<ZipEntry> iterator() {
                return (Iterator<ZipEntry>) zf.entries().asIterator();
            }
        }){
            if(ze.getName().endsWith("/"))
                continue;
            String[] path = ze.getName().split("/");
            OrdinaryFileTree ot = root;
            for(int i = 0; i <= path.length -2; i++){
                ot = ot.getSubdir(path[i]);
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(zf.getInputStream(ze)));
            List<String> content = new ArrayList<>();
            String s;
            while((s = br.readLine()) != null)content.add(s);
            if(ot.putFile(path[path.length-1], content) != null)
                System.err.println("ZipFile "+zf.getName()+" contains "+ze.getName()+" twice!");
        }
        return root;
    }

    private DifferentialFileTree buildDFT(OrdinaryFileTree lft, OrdinaryFileTree rft){
        if(!lft.getName().equals(rft.getName())){
            throw new RuntimeException("Names differ!");
        }
        DifferentialFileTree res = new DifferentialFileTree(lft.getName());
        SetDifference<String> folders = new SetDifference<>(lft.getSubdirs(), rft.getSubdirs());
        folders.getLeftOnly().forEach(f -> res.putLeftOnlySubdir(f, convert(lft.getSubdir(f), true)));
        folders.getRightOnly().forEach(f -> res.putRightOnlySubdir(f, convert(rft.getSubdir(f), false)));
        folders.getCommon().forEach(f -> res.putCommonSubdir(f, buildDFT(lft.getSubdir(f), rft.getSubdir(f))));
        res.setFiles(new DiffedMapDifference<>(lft.getFiles(), rft.getFiles()));
        return res;
    }

    private DifferentialFileTree convert(OrdinaryFileTree singleton, boolean left){
        DifferentialFileTree dft = new DifferentialFileTree(singleton.getName());
        if(left) {
            singleton.getSubdirs().forEach(f -> dft.putLeftOnlySubdir(f, convert(singleton.getSubdir(f), true)));
            dft.setFiles(new DiffedMapDifference<>(singleton.getFiles(), new HashMap<>()));
        }
        else {
            singleton.getSubdirs().forEach(f -> dft.putRightOnlySubdir(f, convert(singleton.getSubdir(f), false)));
            dft.setFiles(new DiffedMapDifference<>(new HashMap<>(), singleton.getFiles()));
        }
        return dft;
    }



}
